# Release notes

## Release notes for 1.6.1-183-203

### General
 * Starting with 1.6.1, Bamboo Soy releases will be IntelliJ API version-dependent

### Features
 * Added support for index syntax in {for} (fixes #228)

### Bug and stability fixes
 * Fixed parsing of the "import" string inside HTML (fixes #231)


## Release notes for 1.6

### Features
 * Added support for list comprehensions (fixed #195)
 * Added proto initialization support (fixes #201)
 * Added support for the {css} and {xid} functional style syntax, {nbsp}, {velog}
 * Added support for {skip}, {key} (fixes #205)
 * Added support for {ifempty} inside {for}
 * Added support for the "import" syntax (fixes #222)
 * Added support for the methods syntax

### Bug and stability fixes
 * HTML block-commenting fixed
 * Missing params with default initializers are no longer reported as errors (fixes #199)
 * Minor bugfixes


## Release notes for 1.5

### Features
 * Added support for {element}, {@state} and default initializers (fixes #172)
 * Added record() type support (fixes #170)
 * Added "Remove unused parameter (state)" quickfix (also removes respective specifications from calls across the project)
 * Added Rename refactoring for variables, @param's and @state's
 * Added folding for template/element, call/delcall and doc comments
 * Added "Comment selection as block"
 * Improved presentation of Soy elements (Structure and Find Usages views)

### Bug and stability fixes
 * Fixed @param parsing (fixes #169)
 * Fixed variable highlighting
 * Fixed Soy file Structure view
 * Improved comments parsing (fixes #164)
 * Minor cosmetic fixes


## Release notes for 1.4

 * General stability and robustness improvements.
 * Added support for Map literals.
 * Fixed formatting of ternary expressions.
 * Fixed issue where the plugin was interfering with non-soy files.
 * Fixed parsing of HTML comments (made non-greedy).
 * Fixed parsing of `{literal}` tag content.


## Release notes for 1.3

 * Formatter: Better support for spacing in and around expressions.
 * General stability and robustness improvements.
 * Fixed ClassCastException occurring when jumping to definitions.
 * More consistent handling of CSS literals.


## Release notes for 1.2
### Features

 * Automatic insertion of closing characters for `"`, `'`, `(` and `[`.
 * Code style settings are now available.
   * Pressing <kbd>Enter</kbd> after an open tag now places the caret at the right indentation level.
 * Formatting improvements:
   * Continuation indent on multiline `alias`.
   * Continuation indent on tag attributes.
   * Improved formatting for deeply nested tags.
 * Live templates for `{let}` and `{let /}`.
 * Autocompletion:
   * Add support for `stricthtml="true"` in template definition blocks.
   * Better context detection for where variable autocompletion should trigger.

### Stability and bug fixes
 * Fixed various NPEs and bugs that occurred on partial/incomplete code constructs.


## Release notes for 1.1

 * Autocompletion now respects block scoping.
 * Performance improvements to parsing and indexing phase.
 * Fixed bug where Soy parser would not correctly delegate to HTML.


## Release notes for 1.0

 * HTML highlighting and completion.
 * Autoformatting.
 * Structure view.
 * Autoclosing of soy tags when typing `{/`.
 * Doc comment support for `@param`, `@inject` and `let` statements.
 * Completion improvements:
     * Kind keyword and supported literals in string literal.
     * Show type in parameter identifier suggestions.
     * Only deltemplates are suggested for delcalls and vice versa for normal templates.
 * Indexing of files is now persisted across restarts.
 * Stability and bug fixes.


## Release notes for alpha-5.

 * Added completion for `visibility="private"` in template open tags.
 * Recognize usage of variables inside string literals, like `{msg desc="$variable"}`.
 * Fix referencing of variables declared in let statements.
 * Disallow referencing `@inject` declarations from template call sites.
 * Fix checking for slash before closing brace on single-tag call statements.
 * Parser accepts index access of parenthesized expressions, like `($foo)[0]`
 * Parser accepts `for` statements with empty body.


## Release notes for alpha-4.

 * Fix issue where identifiers suggestions from let statements would begin with an invalid double-$,
 * Properly recognize map/list access as an expression (it would previously show as a parsing error in certain cases),
 * Remove auto-adding of single quote, it's annoying when typing vanilla text.


## Release notes for alpha-3.

 * Improved alias support:
    * Suggest aliased namespaces in autocompletion results for templates in a `{call}`,
    * Provide autocompletion after using a namespace alias as identifier
 * Adds support for union and record types in parameter definitions,
 * Autocompletion for `kind` of parameters {text, html, uri, attributes, ...},
 * Live template for `{param}` adds `kind="text"` as default based on usage stats.
 * Improvements to live templates:
    * Automatically open completion suggestions for `{param}` and `{alias}` live templates,
    * Renamed the single line `{call}` and `{param}` triggers to scall and sparam to avoid
      colliding with most probable case which is to have open-close tags.


## Release notes for alpha-2.

 * Adds documentation tooltips for references (<kbd>Ctrl</kbd>-hover to show or <kbd>Ctrl-Q</kbd> to
   trigger quick documentation),
 * Adds support for union and record types in parameter definitions,
 * Adds support for alias declaration that omit the alias identifier `{alias my.namespace}`,
 * Adds support for identifiers that are part of identifier expressions. For example in
   `{print($foo.bar.baz)}`, the identifier `$foo` is correctly recognized and will be a reference
   to the site where it’s declared,
 * Fixes parsing issues where `uri` was not allowed as parameter name,
 * Fixes NPE when desugaring aliases.
