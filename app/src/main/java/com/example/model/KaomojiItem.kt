package com.example.model

data class KaomojiItem(
    val text: String,
    val description: String,
    val category: String
)

object KaomojiRepository {
    val CATEGORIES = listOf("Happy & Cute", "Shrug & Table Flip", "Animal & Bears", "Action & Magic", "Sad & Shocked")

    val ITEMS = listOf(
        // Happy & Cute
        KaomojiItem("(◕‿◕)", "Happy smile", "Happy & Cute"),
        KaomojiItem("(｡♥‿♥｡)", "Heart eyes", "Happy & Cute"),
        KaomojiItem("(✿◠‿◠)", "Flower smile", "Happy & Cute"),
        KaomojiItem("(*^ω^*)", "Joyful giggle", "Happy & Cute"),
        KaomojiItem("(´∀｀*)", "Warm blush", "Happy & Cute"),
        KaomojiItem("(≧◡≦)", "Excited cheer", "Happy & Cute"),
        KaomojiItem("(˶ᵔ ᵕ ᵔ˶)", "Soft happy", "Happy & Cute"),
        KaomojiItem("(づ｡◕‿‿◕｡)づ", "Warm hug", "Happy & Cute"),
        KaomojiItem("٩(◕‿◕｡)۶", "Double fist pump", "Happy & Cute"),
        KaomojiItem("(*♡∀♡)", "Love struck", "Happy & Cute"),

        // Shrug & Table Flip
        KaomojiItem("¯\\_(ツ)_/¯", "Classic shrug", "Shrug & Table Flip"),
        KaomojiItem("(╯°□°)╯︵ ┻━┻", "Rage table flip", "Shrug & Table Flip"),
        KaomojiItem("┬─┬ノ( º _ ºノ)", "Put table back politely", "Shrug & Table Flip"),
        KaomojiItem("┻━┻ ︵ ヽ(`Д´)ﾉ ︵ ┻━┻", "Double table toss", "Shrug & Table Flip"),
        KaomojiItem("┐('～`;)┌", "Puzzled shrug", "Shrug & Table Flip"),
        KaomojiItem("┐(￣∀￣)┌", "Carefree shrug", "Shrug & Table Flip"),
        KaomojiItem("(°_o)/¯", "Derp shrug", "Shrug & Table Flip"),

        // Animal & Bears
        KaomojiItem("ʕ•ᴥ•ʔ", "Teddy bear", "Animal & Bears"),
        KaomojiItem("(=^･ω･^=)", "Curious cat", "Animal & Bears"),
        KaomojiItem("(^・x・^)", "Kitty whiskers", "Animal & Bears"),
        KaomojiItem("ʕっ•ᴥ•ʔっ", "Bear hug", "Animal & Bears"),
        KaomojiItem("₍ᐢ. ̫ .⑅ᐢ₎", "Bunny rabbit", "Animal & Bears"),
        KaomojiItem("▼・ᴥ・▼", "Cute doggy", "Animal & Bears"),
        KaomojiItem("(´・(oo)・｀)", "Piggy snout", "Animal & Bears"),
        KaomojiItem("(*￣(エ)￣*)", "Sleepy bear", "Animal & Bears"),

        // Action & Magic
        KaomojiItem("(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧", "Magic sparkle dust", "Action & Magic"),
        KaomojiItem("(ง'̀-'́)ง", "Ready to fight", "Action & Magic"),
        KaomojiItem("ヽ(⌐■_■)ノ♪♬", "Cool shades dancing", "Action & Magic"),
        KaomojiItem("ε=ε=┌(;￣▽￣)┘", "Running away fast", "Action & Magic"),
        KaomojiItem("(☞ﾟヮﾟ)☞", "Finger guns right", "Action & Magic"),
        KaomojiItem("☜(ﾟヮﾟ☜)", "Finger guns left", "Action & Magic"),
        KaomojiItem("┌(・。・)┘♪", "Robot dance", "Action & Magic"),

        // Sad & Shocked
        KaomojiItem("(T_T)", "Tearful crying", "Sad & Shocked"),
        KaomojiItem("(⊙_⊙)", "Wide eyed shock", "Sad & Shocked"),
        KaomojiItem("(;´༎ຶД༎ຶ`)", "Loud ugly crying", "Sad & Shocked"),
        KaomojiItem("(ノ_<。)", "Face palm cry", "Sad & Shocked"),
        KaomojiItem("(°ㅂ°╬)", "Vein popping anger", "Sad & Shocked"),
        KaomojiItem("Σ(°△°|||)", "Stunned panic", "Sad & Shocked")
    )
}
