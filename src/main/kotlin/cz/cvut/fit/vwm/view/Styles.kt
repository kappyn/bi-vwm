package cz.cvut.fit.vwm.view

import kotlinx.css.*

object Styles {
    fun homeCss(builder: CSSBuilder): CSSBuilder = builder.apply {
        body {
            margin(0.px, LinearDimension.auto)
            padding(20.vw)
            textAlign = TextAlign.center

        }
        ul {
            listStyleType = ListStyleType.none
            textAlign = TextAlign.left
        }
        rule("h1.page-title") {
            color = Color.grey
        }
        rule("input[type=search]") {
            padding(10.px, 20.px)
            marginRight = 10.px
            minWidth = 400.px
        }
        rule("input[type=submit]") {
            padding(10.px, 20.px)
        }

    }
}
