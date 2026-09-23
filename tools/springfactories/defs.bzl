"""Rule that merges META-INF/spring.factories across the transitive runtime classpath."""

def _merged_spring_factories_impl(ctx):
    jars = depset(transitive = [dep[JavaInfo].transitive_runtime_jars for dep in ctx.attr.deps])
    out = ctx.actions.declare_file(ctx.label.name + "/META-INF/spring.factories")
    args = ctx.actions.args()
    args.add(out)
    args.add_all(jars)
    ctx.actions.run(
        executable = ctx.executable._merger,
        arguments = [args],
        inputs = jars,
        outputs = [out],
        mnemonic = "MergeSpringFactories",
        progress_message = "Merging spring.factories for %s" % ctx.label,
    )
    return [DefaultInfo(files = depset([out]))]

merged_spring_factories = rule(
    implementation = _merged_spring_factories_impl,
    attrs = {
        "deps": attr.label_list(providers = [JavaInfo]),
        "_merger": attr.label(
            default = "//tools/springfactories:merger",
            executable = True,
            cfg = "exec",
        ),
    },
)
