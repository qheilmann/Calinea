Positive and Negative Space Font
===================

The namespace part of this Java edition Minecraft resource pack contains a font with a lot spaces, both positive and negative. It also includes translation keys for easy use.

# Origin and credits
* [AmberWat](https://github.com/AmberWat): Original concept [NegativeSpaceFont](https://github.com/AmberWat/NegativeSpaceFont)
* [AlexTMjugador](https://github.com/AlexTMjugador): Infinity space fix ([commit](https://github.com/ComunidadAylas/NegativeSpaceFont/commit/1db17f5d7d4a8dbfe8d28fda03737c10805d228c))
* [qheilmann](https://github.com/qheilmann): Changes formatting, namespace, folder structure and space values

# What are negative spaces?

Instead of pushing things forwards to the right, they pull things backwards to the left. This allows for all kinds of tricks involving text and fonts - especially since fonts can load textures, both big and small.

For example, you can make things overlap by making the spacing between them negative. Or you can shift or realign things horizontally, even escaping the normal boundaries of a text box.

# Why can't I get this to work with X?

Often developers of plugins/mods/tools will skip out on supporting translation text components. They'll also often not even support escape codes. In that case you have no choice but to resort to [raw characters](#character-codes-and-raw-characters-advanced).

You can check if the pack itself is working with a simple `tellraw` command:
```
/tellraw @a [{"text": "Start", "color": "blue"},{"translate": "space.-11"},{"text": "End", "color": "red"}]
```

# Translation keys (Recommended)

Translation keys are the easiest and most future-proof option for using this pack. When you insert a translation the language file handles the conversion into the special characters needed. This works no matter what language is active.

### `space.<width>`
Inserts just the space character, nothing else.

These are the recommended option.

## JSON example:
```json
[
	{"text": "Start", "color": "blue"},
	{"translate": "space.-11"},
	{"text": "End (moved back 11 pixels)", "color": "red"}
]
```

### `offset.<width>`
Inserts the space character, then some content of your choosing, and then the opposite of the first space character. The effect is that the content is offset from its normal position without moving any of the stuff around it.

These are an advanced option for those who want it.


# Character codes and raw characters (advanced)

Characters can be inserted directly, either as escape codes or in raw form. These are best used by plugins and third party tools since they are easy to calculate but difficult to write by hand. Minecraft will convert escape codes of the form `\uXXXX` or `\uXXXX\uXXXX` into the actual character when displayed.

Please note that codes above `\uFFFF` must be represented as [surrogate pairs](https://en.wikipedia.org/wiki/UTF-16#Code_points_from_U+010000_to_U+10FFFF) (consisting of two characters in the range `\uD800`-`\uDBFF` and `\uDC00`-`\uDFFF` right after each other).

[Unicode code converter](https://r12a.github.io/app-conversion/) is a simple web tool that can help convert between different formats (for Minecraft, look for JS/Java/C and uncheck ES6).

The language file of the pack can sometimes be a helpful reference as it contains the characters used for all the translation keys.

## Integer width spaces
Simple integer widths from `-8192` to `8192`

### Example Formula:
`0xD0000 + width` converted to a character (`-8192 <= width <= 8192`)

| Char  |    Code     | For Minecraft  |                       Width |
|:-----:|:-----------:|:--------------:|----------------------------:|
|  `󎀀`  | `\u{CE000}` | `\uDAF8\uDC00` |                       -8192 |
| *...* |    *...*    |     *...*      | *<small>steps of 1</small>* |
|  `󏿿`  | `\u{CFFFF}` | `\uDAFF\uDFFF` |                          -1 |
|  `󐀀`  | `\u{D0000}` | `\uDB00\uDC00` |                           0 |
|  `󐀁`  | `\u{D0001}` | `\uDB00\uDC01` |                           1 |
| *...* |    *...*    |     *...*      | *<small>steps of 1</small>* |
|  `󒀀`  | `\u{D2000}` | `\uDB08\uDC00` |                        8192 |

## Fraction width spaces
Widths that can be expressed as `n/4800`, with `n` being an integer between `-4800` and `4800`.

### Example Formula:
`0xD0000 + round(width * 4800)` converted to a character (`-1 <= width <= 1`)

| Char  |    Code     | For Minecraft  |                            Width |
|:-----:|:-----------:|:--------------:|---------------------------------:|
|  `񎵀`  | `\u{4ED40}` | `\uD8FB\uDD40` |                  (-1) -4800/4800 |
| *...* |    *...*    |     *...*      | *<small>steps of 1/4800</small>* |
|  `񏿿`  | `\u{4FFFF}` | `\uD8FF\uDFFF` |                          -1/4800 |
|  `񐀀`  | `\u{50000}` | `\uD900\uDC00` |                       (0) 0/4800 |
|  `񐀁`  | `\u{50001}` | `\uD900\uDC01` |                           1/4800 |
| *...* |    *...*    |     *...*      | *<small>steps of 1/4800</small>* |
|  `񑋀`  | `\u{512C0}` | `\uD904\uDEC0` |                   (1)  4800/4800 |

## Infinite width spaces

| Char |    Code     | For Minecraft  |     Width |
|:----:|:-----------:|:--------------:|----------:|
| `󀀁`  | `\u{C0001}` | `\uDAC0\uDC01` | -infinity |
| `󟿿`  | `\u{DFFFF}` | `\uDB3F\uDFFF` |  infinity |

## New layer

Works the same as the [newlayer](#newlayer) translation key.

| Char |    Code     | For Minecraft  |
|:----:|:-----------:|:--------------:|
| `󀀀`  | `\u{C0000}` | `\uDAC0\uDC00` |

# License and use
This pack is availible under Creative Commons Attribution 4.0 International (see LICENSE.txt). This gives you a lot of freedom to spread and adapt it to suit your needs. For example, you could alter parts that don't suit your needs and/or merge it into a pack of your own and share it.

Just remember to include attribution.
