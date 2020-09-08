package com.adwardstark.letooth.commons

object HexConverter {

    fun convertStringToHex(string: String): String{
        val chars = string.toCharArray()
        val hex = StringBuffer()
        for (index in chars.indices) {
            hex.append(Integer.toHexString(chars[index].toInt()))
        }
        return hex.toString()
    }

    fun convertHexToString(hex: String): String {
        val output = StringBuilder()
        var index = 0
        while (index < hex.length) {
            val str = hex.substring(index, index + 2)
            output.append(Integer.parseInt(str, 16).toChar())
            index += 2
        }
        return output.toString()
    }

}