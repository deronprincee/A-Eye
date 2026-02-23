
data class LogMARRow(val logMAR: Float, val letters: String, val snellen: String)

val realLogMARChart = listOf(
    LogMARRow(1.0f, "DPLOT", "6/60"),
    LogMARRow(0.9f, "ZEFGW", "6/48"),
    LogMARRow(0.8f, "NRTCE", "6/38"),
    LogMARRow(0.7f, "KGHYS", "6/30"),
    LogMARRow(0.6f, "LATEP", "6/24"),
    LogMARRow(0.5f, "OXVBN", "6/18"),
    LogMARRow(0.4f, "FCPRG", "6/15"),
    LogMARRow(0.3f, "BDEKU", "6/12"),
    LogMARRow(0.2f, "MWQTH", "6/9.5"),
    LogMARRow(0.1f, "NQZRP", "6/7.5"),
    LogMARRow(0.0f, "ETDRS", "6/6")
)