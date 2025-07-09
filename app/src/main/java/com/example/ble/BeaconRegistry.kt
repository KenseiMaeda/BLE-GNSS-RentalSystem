object BeaconRegistry {
    private val beaconMap = mapOf(
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 1338, 101) to "予備",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 5) to "筑波病院入口",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 6) to "追越学生宿舎前",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 7) to "平砂学生宿舎前",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 8) to "筑波大学西",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 9) to "大学会館前",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 10) to "第一エリア前",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 11) to "第三エリア前",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 12) to "虹の広場",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 13) to "農林技術センター",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 14) to "一ノ矢学生宿舎前",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 15) to "大学植物見本園",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 16) to "TARAセンター前",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 17) to "筑波大学中央",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 18) to "大学公園",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 19) to "松美池",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 21) to "合宿所",
        Triple("9d52fef3-2bfc-427e-8f25-6c61759eaff6", 29800, 22) to "天久保池"
    )

    fun resolveName(uuid: String, major: Int?, minor: Int?): String {
        return beaconMap[Triple(uuid, major ?: -1, minor ?: -1)] ?: "Unknown"
    }
}
