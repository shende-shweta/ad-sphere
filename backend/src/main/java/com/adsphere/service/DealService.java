package com.adsphere.service;

import com.adsphere.domain.ActivityEvent.Action;
import com.adsphere.domain.ActivityEvent.EntityType;
import com.adsphere.domain.Bid;
import com.adsphere.domain.BidStatus;
import com.adsphere.domain.Campaign;
import com.adsphere.domain.CampaignStatus;
import com.adsphere.domain.Deal;
import com.adsphere.domain.DealStatus;
import com.adsphere.domain.DealType;
import com.adsphere.dto.BidRequest;
import com.adsphere.dto.BidResponse;
import com.adsphere.dto.DealResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.repository.BidRepository;
import com.adsphere.repository.CampaignRepository;
import com.adsphere.repository.DealRepository;
import com.adsphere.repository.Specs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory deals and bidding.
 *
 * <p>Each deal has a floor price (CPM). A bid at or above the floor wins immediately: the deal is
 * marked sold and the bidding campaign's ads are served on that inventory. Bids below the floor are
 * recorded as rejected.
 */
@Service
@Transactional
public class DealService {

  private final DealRepository deals;
  private final BidRepository bids;
  private final CampaignRepository campaigns;
  private final ActivityService activityService;

  public DealService(DealRepository deals, BidRepository bids, CampaignRepository campaigns,
      ActivityService activityService) {
    this.deals = deals;
    this.bids = bids;
    this.campaigns = campaigns;
    this.activityService = activityService;
  }

  @Transactional(readOnly = true)
  public PageResponse<DealResponse> search(
      String query, DealType type, DealStatus status, Pageable pageable) {
    Specification<Deal> spec =
        Specification.where(Specs.<Deal>containsText(query, "name", "publisher"))
            .and(Specs.equalTo("type", type))
            .and(Specs.equalTo("status", status));
    return PageResponse.of(deals.findAll(spec, pageable), DealResponse::from);
  }

  @Transactional(readOnly = true)
  public DealResponse get(Long id) {
    return DealResponse.from(deals.findById(id).orElseThrow(() -> notFound(id)));
  }

  public BidResponse placeBid(Long dealId, BidRequest request, String bidder) {
    Deal deal = deals.findByIdForUpdate(dealId).orElseThrow(() -> notFound(dealId));
    if (deal.getStatus() == DealStatus.SOLD) {
      throw BusinessRuleException.conflict("This deal has already been sold");
    }
    if (deal.getStatus() != DealStatus.ACTIVE) {
      throw BusinessRuleException.conflict("This deal is paused and not accepting bids");
    }
    Campaign campaign =
        campaigns
            .findById(request.campaignId)
            .orElseThrow(() -> BusinessRuleException.field("campaignId", "Campaign does not exist"));
    if (campaign.getStatus() == CampaignStatus.COMPLETED) {
      throw BusinessRuleException.field("campaignId", "Completed campaigns cannot bid");
    }

    boolean wins = request.amount.compareTo(deal.getBidPrice()) >= 0;
    Bid bid =
        bids.save(
            new Bid(deal, campaign, bidder, request.amount, wins ? BidStatus.WON : BidStatus.REJECTED));
    String message;
    if (wins) {
      deal.sell(request.amount, bidder, campaign);
      message =
          String.format(
              "Bid won. \"%s\" is sold and ads for \"%s\" will now be displayed.",
              deal.getName(), campaign.getName());
    } else {
      message =
          String.format(
              "Bid rejected: $%s is below the floor price of $%s.",
              request.amount.toPlainString(), deal.getBidPrice().toPlainString());
    }
    activityService.record(Action.BID_PLACED, EntityType.DEAL,
        deal.getId(), deal.getName(),
        "Bid of $" + request.amount.toPlainString() + " placed on deal \""
            + deal.getName() + "\" - " + (wins ? "won" : "rejected"));
    return new BidResponse(bid.getId(), bid.getStatus(), bid.getAmount(), message, DealResponse.from(deal));
  }

  private static NotFoundException notFound(Long id) {
    return new NotFoundException("Deal", id);
  }
}
