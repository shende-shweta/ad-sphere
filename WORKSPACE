workspace(name = "adsphere")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/jdk:remote_java_repository.bzl", "remote_java_repository")

# --- Hermetic JDK 21 -------------------------------------------------------------------------
# Bazel 6.4's bundled rules_java only ships JDKs up to 17, so JDK 21 runtimes are registered here.
# Selected by --java_runtime_version=remotejdk_21 / --tool_java_runtime_version=remotejdk_21
# (see .bazelrc). Application bytecode still targets Java 11.

JDK21_ZULU = "zulu21.36.17-ca-jdk21.0.4"

[
    remote_java_repository(
        name = "remotejdk21_" + platform,
        prefix = "remotejdk",
        sha256 = sha256,
        strip_prefix = "%s-%s" % (JDK21_ZULU, archive),
        target_compatible_with = constraints,
        urls = ["https://cdn.azul.com/zulu/bin/%s-%s.tar.gz" % (JDK21_ZULU, archive)],
        version = "21",
    )
    for platform, archive, sha256, constraints in [
        ("linux", "linux_x64", "318d0c2ed3c876fb7ea2c952945cdcf7decfb5264ca51aece159e635ac53d544", ["@platforms//os:linux", "@platforms//cpu:x86_64"]),
        ("linux_aarch64", "linux_aarch64", "da3c2d7db33670bcf66532441aeb7f33dcf0d227c8dafe7ce35cee67f6829c4c", ["@platforms//os:linux", "@platforms//cpu:aarch64"]),
        ("macos", "macosx_x64", "5ce75a6a247c7029b74c4ca7cf6f60fd2b2d68ce1e8956fb448d2984316b5fea", ["@platforms//os:macos", "@platforms//cpu:x86_64"]),
        ("macos_aarch64", "macosx_aarch64", "bc2750f81a166cc6e9c30ae8aaba54f253a8c8ec9d8cfc04a555fe20712c7bff", ["@platforms//os:macos", "@platforms//cpu:aarch64"]),
    ]
]

# --- Maven dependencies ----------------------------------------------------------------------

RULES_JVM_EXTERNAL_TAG = "5.3"

RULES_JVM_EXTERNAL_SHA = "d31e369b854322ca5098ea12c69d7175ded971435e55c18dd9dd5f29cc5249ac"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG),
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")

SPRING_BOOT_VERSION = "2.1.0.RELEASE"

maven_install(
    artifacts = [
        "org.springframework.boot:spring-boot-starter-web:" + SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-data-jpa:" + SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-security:" + SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-validation:" + SPRING_BOOT_VERSION,
        "org.springframework.boot:spring-boot-starter-actuator:" + SPRING_BOOT_VERSION,
        "com.h2database:h2:1.4.197",
        "io.jsonwebtoken:jjwt:0.9.1",
        "javax.xml.bind:jaxb-api:2.3.1",
        # Byte Buddy shipped with Boot 2.1 predates JDK 21 class files; pin a JDK 21 aware release.
        "net.bytebuddy:byte-buddy:1.14.9",
        "net.bytebuddy:byte-buddy-agent:1.14.9",
        # Test
        "org.springframework.boot:spring-boot-starter-test:" + SPRING_BOOT_VERSION,
        "org.springframework.security:spring-security-test:5.1.1.RELEASE",
    ],
    fetch_sources = False,
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
    version_conflict_policy = "pinned",
)
