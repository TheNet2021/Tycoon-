package com.example.ui.theme

data class AppLanguageOption(
    val code: String,
    val nativeLabel: String
)

val SupportedLanguages = listOf(
    AppLanguageOption("en", "English"),
    AppLanguageOption("es", "Español"),
    AppLanguageOption("fr", "Français"),
    AppLanguageOption("de", "Deutsch"),
    AppLanguageOption("pt", "Português"),
    AppLanguageOption("ar", "العربية")
)

data class LocalizedLabels(
    val continueBtn: String,
    val startGameBtn: String,
    val newGameBtn: String,
    val worldMapBtn: String,
    val profileBtn: String,
    val achievementsBtn: String,
    val marketBtn: String,
    val settingsBtn: String,
    val saveSlotsBtn: String,
    val loadingWorldTitle: String,
    val loadingWorldSubtitle: String,
    val confirmNewGameTitle: String,
    val confirmNewGameBody: String,
    val confirmOverwriteBtn: String,
    val cancelBtn: String,
    val settingsTitle: String,
    val resetSettingsBtn: String,
    val profileTitle: String
)

fun localizedLabelsFor(code: String): LocalizedLabels = when (code) {
    "es" -> LocalizedLabels(
        continueBtn = "CONTINUAR",
        startGameBtn = "INICIAR",
        newGameBtn = "NUEVA PARTIDA",
        worldMapBtn = "MAPA MUNDIAL",
        profileBtn = "PERFIL",
        achievementsBtn = "LOGROS",
        marketBtn = "MERCADO",
        settingsBtn = "AJUSTES",
        saveSlotsBtn = "Ranuras de Guardado",
        loadingWorldTitle = "Cargando Expedición...",
        loadingWorldSubtitle = "Restaurando región, edificios y recursos",
        confirmNewGameTitle = "¿Iniciar Nueva Partida?",
        confirmNewGameBody = "Esto sobrescribirá la expedición guardada en la ranura actual. Tu progreso actual se perderá.",
        confirmOverwriteBtn = "Nueva Partida",
        cancelBtn = "Cancelar",
        settingsTitle = "Ajustes y Audio",
        resetSettingsBtn = "Restablecer Ajustes",
        profileTitle = "Perfil del Pionero"
    )
    "fr" -> LocalizedLabels(
        continueBtn = "CONTINUER",
        startGameBtn = "JOUER",
        newGameBtn = "NOUVELLE PARTIE",
        worldMapBtn = "CARTE DU MONDE",
        profileBtn = "PROFIL",
        achievementsBtn = "SUCCÈS",
        marketBtn = "MARCHÉ",
        settingsBtn = "PARAMÈTRES",
        saveSlotsBtn = "Emplacements",
        loadingWorldTitle = "Chargement du Monde...",
        loadingWorldSubtitle = "Restauration de la carte, des bâtiments et des ressources",
        confirmNewGameTitle = "Nouvelle Partie ?",
        confirmNewGameBody = "Cela remplacera la sauvegarde de cet emplacement. Votre progression actuelle sera perdue.",
        confirmOverwriteBtn = "Nouvelle Partie",
        cancelBtn = "Annuler",
        settingsTitle = "Paramètres & Audio",
        resetSettingsBtn = "Réinitialiser les Paramètres",
        profileTitle = "Profil du Pionnier"
    )
    "de" -> LocalizedLabels(
        continueBtn = "FORTSETZEN",
        startGameBtn = "STARTEN",
        newGameBtn = "NEUES SPIEL",
        worldMapBtn = "WELTKARTE",
        profileBtn = "PROFIL",
        achievementsBtn = "ERFOLGE",
        marketBtn = "MARKT",
        settingsBtn = "EINSTELLUNGEN",
        saveSlotsBtn = "Spielstände",
        loadingWorldTitle = "Welt wird geladen...",
        loadingWorldSubtitle = "Region, Gebäude & Ressourcen werden wiederhergestellt",
        confirmNewGameTitle = "Neues Spiel starten?",
        confirmNewGameBody = "Dadurch wird dein aktueller Spielstand in diesem Slot überschrieben.",
        confirmOverwriteBtn = "Neues Spiel",
        cancelBtn = "Abbrechen",
        settingsTitle = "Einstellungen & Audio",
        resetSettingsBtn = "Einstellungen zurücksetzen",
        profileTitle = "Pionier-Profil"
    )
    "pt" -> LocalizedLabels(
        continueBtn = "CONTINUAR",
        startGameBtn = "COMEÇAR",
        newGameBtn = "NOVO JOGO",
        worldMapBtn = "MAPA MUNDIAL",
        profileBtn = "PERFIL",
        achievementsBtn = "CONQUISTAS",
        marketBtn = "MERCADO",
        settingsBtn = "CONFIGURAÇÕES",
        saveSlotsBtn = "Slots de Save",
        loadingWorldTitle = "Carregando Mundo...",
        loadingWorldSubtitle = "Restaurando bioma, construções e recursos",
        confirmNewGameTitle = "Iniciar Novo Jogo?",
        confirmNewGameBody = "Isso substituirá a expedição salva neste slot. Seu progresso atual será perdido.",
        confirmOverwriteBtn = "Novo Jogo",
        cancelBtn = "Cancelar",
        settingsTitle = "Configurações e Áudio",
        resetSettingsBtn = "Redefinir Configurações",
        profileTitle = "Perfil do Pioneiro"
    )
    "ar" -> LocalizedLabels(
        continueBtn = "متابعة",
        startGameBtn = "ابدأ اللعب",
        newGameBtn = "لعبة جديدة",
        worldMapBtn = "خريطة العالم",
        profileBtn = "الملف الشخصي",
        achievementsBtn = "الإنجازات",
        marketBtn = "السوق",
        settingsBtn = "الإعدادات",
        saveSlotsBtn = "خانات الحفظ",
        loadingWorldTitle = "جارٍ تحميل العالم...",
        loadingWorldSubtitle = "استعادة الخريطة والمباني والموارد",
        confirmNewGameTitle = "بدء لعبة جديدة؟",
        confirmNewGameBody = "سيتم استبدال التقدم المحفوظ في هذه الخانة. هل تريد المتابعة؟",
        confirmOverwriteBtn = "لعبة جديدة",
        cancelBtn = "إلغاء",
        settingsTitle = "الإعدادات والصوت",
        resetSettingsBtn = "إعادة ضبط الإعدادات",
        profileTitle = "ملف المستكشف"
    )
    else -> LocalizedLabels(
        continueBtn = "CONTINUE",
        startGameBtn = "START EXPEDITION",
        newGameBtn = "NEW GAME",
        worldMapBtn = "WORLD MAP",
        profileBtn = "PROFILE",
        achievementsBtn = "ACHIEVEMENTS",
        marketBtn = "MARKET",
        settingsBtn = "SETTINGS",
        saveSlotsBtn = "Save Slots",
        loadingWorldTitle = "Loading Frontier World...",
        loadingWorldSubtitle = "Restoring biome, buildings & resources",
        confirmNewGameTitle = "Start New Game?",
        confirmNewGameBody = "This will overwrite the saved expedition in your active slot. You can switch to another save slot instead.",
        confirmOverwriteBtn = "Start New Game",
        cancelBtn = "Cancel",
        settingsTitle = "Settings & Audio",
        resetSettingsBtn = "Reset Settings to Default",
        profileTitle = "Pioneer Profile"
    )
}
