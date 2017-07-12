# Bamboo Soy for IntelliJ 🏮🍣🏮

The smartest Intellij plugin for the [Soy templating language](https://github.com/google/closure-templates).

![output](https://user-images.githubusercontent.com/16721021/28110334-f51cae42-66e9-11e7-9ae2-211d3acf12fd.gif)

Adds syntax highlighting, autocompletion and static analysis for your closure template files.

## Installation
Install the plugin directly from your IDE or the
[Jetbrains plugin repository](https://plugins.jetbrains.com/plugin/9841-bamboo-soy).

## Feature Summary

Bamboo Soy aims to provide super-fast, no-compromise language support for Soy in IntelliJ.

 * Syntax highlighting with complete HTML support,
 * References, go-to definition, find usage,
 * Structure view,
 * Auto-formatting,
 * Documentation lookup,
 * YCM-style autocompletion (just press <kbd>Ctrl</kbd>-<kbd>Space</kbd> anywhere to get the suggestions)
   * Template and namespace identifiers,
   * Identifiers in scope,
   * Parameters and types,
   * Keywords and literals,
 * Live templates (snippets) for `xid`, `css`, `if-else`, `call`, `delcall`, ...
 * Static analysis inspections for
   * Missing required or invalid parameters,
   * Unused parameters or variables,
   * Wrong usage of double quotes for Soy strings,
 * Understands and preserves doc comment structure on enter,
 * Latest syntax support (`@inject`, `xid` and `css` function expressions, ...),

As a bonus, the parser was designed to support incomplete code constructs &
unbalanced tags (things don’t break when you type).

## Release notes

For the complete history, see the [release notes page](releasenotes.md).

### Release notes for alpha-5.

 * Added completion for `visibility="private"` in template open tags.
 * Recognize usage of variables inside string literals, like `{msg desc="$variable"}`.
 * Fix referencing of variables declared in let statements.
 * Disallow referencing `@inject` declarations from template call sites.
 * Fix checking for slash before closing brace on single-tag call statements.
 * Parser accepts index access of parenthesized expressions, like `($foo)[0]`
 * Parser accepts `for` statements with empty body.

## Contributing

Small and large contributions welcome! For new features or substantial changes, please open an issue
beforehand so that it can be discussed.

For all the details, see the [contributing page](CONTRIBUTING.md).
