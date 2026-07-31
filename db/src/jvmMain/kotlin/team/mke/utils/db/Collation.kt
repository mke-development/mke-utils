package team.mke.utils.db

object Collation {
    @Deprecated("use utf8mb4_uca1400_ai_ci for new mariaDB versions (10.0+)", ReplaceWith("utf8mb4_uca1400_ai_ci"))
    val utf8mb4_unicode_520_ci = "utf8mb4_unicode_520_ci"
    val utf8mb4_uca1400_ai_ci = "utf8mb4_uca1400_ai_ci"
    val utf8mb4_uca1400_as_ci = "utf8mb4_uca1400_as_ci"
    val utf8mb4_bin = "utf8mb4_bin"
    val ascii_bin = "ascii_bin"
}
