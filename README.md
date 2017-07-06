# Bamboo Soy

The smartest Intellij plugin for Soy. 

Adds syntax highlighting, autocompletion and static analysis for your closure template files.

## Features

 * Syntax highlighting, including full HTML support
 * Parser designed to support incomplete code constructs & unbalanced tags (things don’t break when you type)
 * Supports latest syntax (`@inject`, `xid` and `css` function expressions, ...)
 * References, go-to definition
 * Documentation lookup
 * Autocompletion
   * Template and namespace identifiers 
   * Variable identifiers in scope
 * Live templates for `xid`, `css`, `if-else`, `call`, `delcall`, ... 
 * Enter handler in comments and after opening tags to preserve structure and correct indentation of code
 * Inspections - Static analysis for
   * Missing required or invalid parameters
   * Unused parameters

## Release notes

### Release notes for alpha-5.

 * Added completion for `visibility="private"` in template open tags.
 * Recognize usage of variables inside string literals, like `{msg desc="$variable"}`.
 * Fix referencing of variables declared in let statements.
 * Disallow referencing `@inject` declarations from template call sites.
 * Fix checking for slash before closing brace on single-tag call statements.
 * Parser accepts index access of parenthesized expressions, like `($foo)[0]`
 * Parser accepts `for` statements with empty body.

### Release notes for alpha-4.

 * Fix issue where identifiers suggestions from let statements would begin with an invalid double-$,
 * Properly recognize map/list access as an expression (it would previously show as a parsing error in certain cases),
 * Remove auto-adding of single quote, it's annoying when typing vanilla text.

### Release notes for alpha-3.

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

### Release notes for alpha-2.

 * Adds documentation tooltips for references (<kbd>Ctrl</kbd>-hover to show or <kbd>Ctrl-Q</kbd> to
   trigger quick documentation),
 * Adds support for union and record types in parameter definitions,
 * Adds support for alias declaration that omit the alias identifier `{alias my.namespace}`,
 * Adds support for identifiers that are part of identifier expressions. For example in
   `{print($foo.bar.baz)}`, the identifier `$foo` is correctly recognized and will be a reference
   to the site where it’s declared,
 * Fixes parsing issues where `uri` was not allowed as parameter name,
 * Fixes NPE when desugaring aliases.
