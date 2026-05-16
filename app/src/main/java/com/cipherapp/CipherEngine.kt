package com.cipherapp

object CipherEngine {

    private val encryptMap: Map<Char, Char> = mapOf(
        ' ' to ' ', '!' to 'V', '"' to ':', '#' to '<',
        '$' to 'm', '%' to '7', '&' to '@', '\'' to ',',
        '(' to 'z', ')' to '(', '*' to 'p', '+' to '[',
        ',' to '4', '-' to 't', '.' to 'a', '/' to 'T',
        '0' to 'Q', '1' to '}', '2' to ']', '3' to 'G',
        '4' to '"', '5' to 'b', '6' to ';', '7' to 'l',
        '8' to 'A', '9' to '.', ':' to '2', ';' to 'u',
        '<' to 'h', '=' to 'c', '>' to '3', '?' to '$',
        '@' to 'M', 'A' to 's', 'B' to 'o', 'C' to 'k',
        'D' to 'R', 'E' to '>', 'F' to '+', 'G' to 'O',
        'H' to 'X', 'I' to 'N', 'J' to '\\', 'K' to 'q',
        'L' to '*', 'M' to '1', 'N' to 'j', 'O' to 'K',
        'P' to '=', 'Q' to 'H', 'R' to '&', 'S' to '?',
        'T' to '^', 'U' to 'x', 'V' to 'I', 'W' to 'P',
        'X' to 'd', 'Y' to 'r', 'Z' to 'C', '[' to '8',
        ']' to 'v', '^' to 'W', '_' to 'Z', '`' to '#',
        'a' to 'E', 'b' to 'Y', 'c' to 'w', 'd' to '~',
        'e' to 'i', 'f' to 'D', 'g' to '`', 'h' to 'U',
        'i' to '6', 'j' to '|', 'k' to 'S', 'l' to 'y',
        'm' to '5', 'n' to 'L', 'o' to '/', 'p' to '{',
        'q' to 'f', 'r' to 'F', 's' to 'B', 't' to '%',
        'u' to '_', 'v' to '0', 'w' to ')', 'x' to '-',
        'y' to '!', 'z' to '9', '{' to 'g', '|' to 'n',
        '}' to 'e', '~' to 'J'
    )

    private val decryptMap = encryptMap.entries.associate { (p, c) -> c to p }

    fun encrypt(input: String) = buildString {
        for (ch in input) append(encryptMap[ch] ?: ch)
    }

    fun decrypt(input: String) = buildString {
        for (ch in input) append(decryptMap[ch] ?: ch)
    }
}
