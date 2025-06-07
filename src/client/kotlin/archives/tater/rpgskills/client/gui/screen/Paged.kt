package archives.tater.rpgskills.client.gui.screen

interface Paged {
    /**
     * The implementer is responsible for preventing overflow
     */
    var selectedPage: Int
}