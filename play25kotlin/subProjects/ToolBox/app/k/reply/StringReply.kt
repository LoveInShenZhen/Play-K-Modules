package k.reply

import k.aop.annotations.Comment


/**
 * Created by kk on 14-10-7.
 */

class StringReply : ReplyBase() {

    @Comment("文本内容")
    var result: String? = null
}

