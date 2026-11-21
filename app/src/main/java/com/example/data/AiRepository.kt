package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.R
import kotlinx.coroutines.delay

enum class ConceptImageSize(val label: String, val pixelDim: Int) {
    SIZE_1K("1K", 1024),
    SIZE_2K("2K", 2048),
    SIZE_4K("4K", 4096)
}

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isSampleNotice: Boolean = false
)

data class GeneratedConceptResult(
    val bitmap: Bitmap,
    val prompt: String,
    val size: ConceptImageSize,
    val isSamplePlaceholder: Boolean
)

interface AiRepository {
    val isFirebaseEnabled: Boolean
    val systemInstruction: String

    suspend fun sendAdvisorMessage(
        history: List<ChatMessage>,
        userMessage: String,
        gameContextSummary: String
    ): Result<String>

    suspend fun generateConceptImage(
        prompt: String,
        size: ConceptImageSize
    ): Result<GeneratedConceptResult>
}

/**
 * Placeholder implementation used when Firebase setup was declined by the user.
 * Clearly labels every response as a sample preview and never calls a direct API key.
 */
class PlaceholderAiRepository(private val context: Context) : AiRepository {

    override val isFirebaseEnabled: Boolean = false

    override val systemInstruction: String =
        "You are the Wildcraft Tycoon Master Strategist. Give concise, actionable advice on " +
            "tool upgrades, factory multipliers (Sawmill, Smelter, Warehouse), auto-miners, and " +
            "biome unlocks tailored to the player's current coin balance and gear."

    override suspend fun sendAdvisorMessage(
        history: List<ChatMessage>,
        userMessage: String,
        gameContextSummary: String
    ): Result<String> {
        delay(350)
        val q = userMessage.lowercase()
        val tip = when {
            "upgrade" in q || "next" in q || "buy" in q ->
                "Based on your current snapshot ($gameContextSummary), prioritize Backpack capacity " +
                    "so you spend more time gathering before returning to the Trading Post, then level " +
                    "your Axe/Pickaxe to speed up node breaks and unlock the Rocky Mountains ($150) for Iron & Coal."
            "gold" in q || "biome" in q || "region" in q || "unlock" in q ->
                "Biome progression order: Green Forest (Wood/Stone) -> Rocky Mountains ($150, Iron/Coal) -> " +
                    "Golden Desert ($650, Copper/Gold) -> Frozen Lands ($2,000, Crystals) -> Volcanic Region ($6,500, Magma Cores) -> " +
                    "Ancient Jungle ($18,000, Rare Relics). Golden Desert is your first big jump for Gold veins!"
            "factory" in q || "sawmill" in q || "smelter" in q || "passive" in q ->
                "Build the Timber Sawmill (+45% Wood value) and Forest Lumber Bot early for steady passive income, " +
                    "then unlock Rocky Mountains to build the Ore Smelter (+45% mineral value per level). " +
                    "Auto-miners keep earning even while you're offline!"
            else ->
                "Frontier Tip: Walk near trees or mineral veins to auto-gather, and step onto the golden " +
                    "Trading Post pad to stream your backpack into coins. Your current state: $gameContextSummary."
        }
        return Result.success("[Sample Advisor Preview — Enable Firebase for live Gemini]\n\n$tip")
    }

    override suspend fun generateConceptImage(
        prompt: String,
        size: ConceptImageSize
    ): Result<GeneratedConceptResult> {
        delay(500)
        val rawBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_sample_concept_1795274801921
        ) ?: return Result.failure(IllegalStateException("Sample concept drawable missing."))
        return Result.success(
            GeneratedConceptResult(
                bitmap = rawBitmap,
                prompt = prompt,
                size = size,
                isSamplePlaceholder = true
            )
        )
    }
}
