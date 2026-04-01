package com.musicai.util

object JsonLoader {
    fun load(filename: String): String =
        JsonLoader::class.java.classLoader!!
            .getResourceAsStream(filename)!!
            .bufferedReader()
            .readText()
}
