package com.owoh.video.media.bgmusic

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/23 15:39
 */
class ItemMusic {

    /**
     * id : 1
     * music :
     * name_en : No BGM
     * name_cn : 无
     * name_tw : 無
     * image : https://res.owohpet.cn/c/owoh-video/music/none.png
     */

    private var id: Int = 0
    private var music: String? = null
    private var name_en: String? = null
    private var name_cn: String? = null
    private var name_tw: String? = null
    private var image: String? = null

    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getMusic(): String? {
        return music
    }

    fun setMusic(music: String) {
        this.music = music
    }

    fun getName_en(): String? {
        return name_en
    }

    fun setName_en(name_en: String) {
        this.name_en = name_en
    }

    fun getName_cn(): String? {
        return name_cn
    }

    fun setName_cn(name_cn: String) {
        this.name_cn = name_cn
    }

    fun getName_tw(): String? {
        return name_tw
    }

    fun setName_tw(name_tw: String) {
        this.name_tw = name_tw
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String) {
        this.image = image
    }



    companion object {
//        val jason = "[{\"id\":1,\"music\":\"\",\"name_en\":\"No BGM\",\"name_cn\":\"无\",\"name_tw\":\"無\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/none.png\"},{\"id\":2,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/train.mp3\",\"name_en\":\"Train\",\"name_cn\":\"小火车\",\"name_tw\":\"小火車\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":3,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/hcny.mp3\",\"name_en\":\"HCNY\",\"name_cn\":\"新年快乐\",\"name_tw\":\"新年快樂\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":4,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/water.mp3\",\"name_en\":\"Water\",\"name_cn\":\"水中嬉戏\",\"name_tw\":\"水中嬉戲\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":6,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/clown.mp3\",\"name_en\":\"Clown\",\"name_cn\":\"可爱的小丑\",\"name_tw\":\"可愛的小丑\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":7,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/fluffy.mp3\",\"name_en\":\"Cloud\",\"name_cn\":\"软软的云\",\"name_tw\":\"軟軟的雲\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":8,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/my_year.mp3\",\"name_en\":\"My Year\",\"name_cn\":\"红红的年\",\"name_tw\":\"红红的年\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":9,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/planet.mp3\",\"name_en\":\"Planet\",\"name_cn\":\"快乐行星\",\"name_tw\":\"快樂行星\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":10,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/doggies.mp3\",\"name_en\":\"Doggies\",\"name_cn\":\"来吧，小牛犊\",\"name_tw\":\"來吧，小牛犢\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":11,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/piano.mp3\",\"name_en\":\"Piano\",\"name_cn\":\"玩具钢琴\",\"name_tw\":\"玩具鋼琴\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":12,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/playground.mp3\",\"name_en\":\"Playground\",\"name_cn\":\"游乐园\",\"name_tw\":\"遊樂園\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"}]"
//        val jason = "[{\"id\":1,\"music\":\"\",\"name_en\":\"No BGM\",\"name_cn\":\"无\",\"name_tw\":\"無\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/none.png\"},{\"id\":2,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/train.mp3\",\"name_en\":\"Train\",\"name_cn\":\"小火车\",\"name_tw\":\"小火車\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":3,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/hcny.mp3\",\"name_en\":\"HCNY\",\"name_cn\":\"新年快乐\",\"name_tw\":\"新年快樂\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":4,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/water.mp3\",\"name_en\":\"Water\",\"name_cn\":\"水中嬉戏\",\"name_tw\":\"水中嬉戲\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":6,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/clown.mp3\",\"name_en\":\"Clown\",\"name_cn\":\"可爱的小丑\",\"name_tw\":\"可愛的小丑\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":7,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/fluffy.mp3\",\"name_en\":\"Cloud\",\"name_cn\":\"软软的云\",\"name_tw\":\"軟軟的雲\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":8,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/my_year.mp3\",\"name_en\":\"My Year\",\"name_cn\":\"红红的年\",\"name_tw\":\"红红的年\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":9,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/planet.mp3\",\"name_en\":\"Planet\",\"name_cn\":\"快乐行星\",\"name_tw\":\"快樂行星\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":10,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/doggies.mp3\",\"name_en\":\"Doggies\",\"name_cn\":\"来吧，小牛犊\",\"name_tw\":\"來吧，小牛犢\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":11,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/piano.mp3\",\"name_en\":\"Piano\",\"name_cn\":\"玩具钢琴\",\"name_tw\":\"玩具鋼琴\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"},{\"id\":12,\"music\":\"https://res.owohpet.cn/c/owoh-video/music/playground.mp3\",\"name_en\":\"Playground\",\"name_cn\":\"游乐园\",\"name_tw\":\"遊樂園\",\"image\":\"https://res.owohpet.cn/c/owoh-video/music/music.png\"}]"
        val jason = "[{\n" +
        "\t\"id\": 1,\n" +
        "\t\"music\": \"\",\n" +
        "\t\"name_en\": \"No BGM\",\n" +
        "\t\"name_cn\": \"无\",\n" +
        "\t\"name_tw\": \"無\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/none.png\"\n" +
        "}, {\n" +
        "\t\"id\": 2,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/train.mp3\",\n" +
        "\t\"name_en\": \"Train\",\n" +
        "\t\"name_cn\": \"小火车\",\n" +
        "\t\"name_tw\": \"小火車\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 3,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/hcny.mp3\",\n" +
        "\t\"name_en\": \"HCNY\",\n" +
        "\t\"name_cn\": \"新年快乐\",\n" +
        "\t\"name_tw\": \"新年快樂\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 4,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/water.mp3\",\n" +
        "\t\"name_en\": \"Water\",\n" +
        "\t\"name_cn\": \"水中嬉戏\",\n" +
        "\t\"name_tw\": \"水中嬉戲\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 6,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/clown.mp3\",\n" +
        "\t\"name_en\": \"Clown\",\n" +
        "\t\"name_cn\": \"可爱的小丑\",\n" +
        "\t\"name_tw\": \"可愛的小丑\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 7,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/fluffy.mp3\",\n" +
        "\t\"name_en\": \"Cloud\",\n" +
        "\t\"name_cn\": \"软软的云\",\n" +
        "\t\"name_tw\": \"軟軟的雲\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 8,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/my_year.mp3\",\n" +
        "\t\"name_en\": \"My Year\",\n" +
        "\t\"name_cn\": \"红红的年\",\n" +
        "\t\"name_tw\": \"红红的年\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 9,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/planet.mp3\",\n" +
        "\t\"name_en\": \"Planet\",\n" +
        "\t\"name_cn\": \"快乐行星\",\n" +
        "\t\"name_tw\": \"快樂行星\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 10,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/doggies.mp3\",\n" +
        "\t\"name_en\": \"Doggies\",\n" +
        "\t\"name_cn\": \"来吧，小牛犊\",\n" +
        "\t\"name_tw\": \"來吧，小牛犢\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 11,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/piano.mp3\",\n" +
        "\t\"name_en\": \"Piano\",\n" +
        "\t\"name_cn\": \"玩具钢琴\",\n" +
        "\t\"name_tw\": \"玩具鋼琴\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}, {\n" +
        "\t\"id\": 12,\n" +
        "\t\"music\": \"/storage/emulated/0/OwOh/download/music/bg/playground.mp3\",\n" +
        "\t\"name_en\": \"Playground\",\n" +
        "\t\"name_cn\": \"游乐园\",\n" +
        "\t\"name_tw\": \"遊樂園\",\n" +
        "\t\"image\": \"https://res.owohpet.cn/c/owoh-video/music/music.png\"\n" +
        "}]"
    }
}
