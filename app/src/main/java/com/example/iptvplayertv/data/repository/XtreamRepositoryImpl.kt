class XtreamRepositoryImpl(
    private val api: XtreamApi
) : XtreamRepository {

    override suspend fun login(
        host: String,
        user: String,
        pass: String
    ): Boolean {

        val response = api.authenticate(
            username = user,
            password = pass
        )

        return response.user_info.status == "Active"
    }
}
