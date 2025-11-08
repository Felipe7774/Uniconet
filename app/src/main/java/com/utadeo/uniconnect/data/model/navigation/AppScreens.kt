package com.utadeo.uniconnect.data.model.navigation

// Clase sellada que define todas las rutas de navegación de la app
sealed class AppScreens(val route: String) {

    // 🟢 Pantalla inicial / Splash
    object SplashScreen : AppScreens("splash_screen")

    // 🟠 Autenticación y registro
    object LoginScreen : AppScreens("login_screen")
    object LoginWithPasswordScreen : AppScreens("login_with_password_screen")
    object RegisterScreen : AppScreens("register_screen")

    object RegisterWithPasswordScreen : AppScreens("register_with_password_screen/{email}") {
        fun createRoute(email: String): String = "register_with_password_screen/$email"
    }

    // 🏠 Pantalla principal (Home)
    object HomeScreen : AppScreens("home_screen")

    // ⚙️ Configuración y perfil
    object SettingsScreen : AppScreens("settings_screen")
    object ProfilePictureScreen : AppScreens("profile_picture_screen")
    object ProfileCreationScreen : AppScreens("profile_creation_screen")
    object IdentityVerification : AppScreens("identity_verification")

    // 👤 Perfil del usuario
    object UserProfileScreen : AppScreens("user_profile_screen/{userId}") {
        fun createRoute(userId: String) = "user_profile_screen/$userId"
        fun myProfile() = "user_profile_screen/me"
    }

    // 💬 Chat
    object ChatScreen : AppScreens("chat_screen/{chatId}") {
        fun createRoute(chatId: String): String = "chat_screen/$chatId"
    }

    // 💬 Lista de chats individuales
    object ChatsListScreen : AppScreens("chats_list")

    // 💬 Chat individual con otro usuario
    object IndividualChatScreen {
        const val route = "individual_chat"
        fun createRoute(chatId: String, otherUserId: String, otherUserName: String): String {
            return "$route/$chatId/$otherUserId/$otherUserName"
        }
    }


    // 🟡 Flujo de actividades
    object ActivityFlowScreen : AppScreens("activity_flow_screen")

    object ActivityStepScreen : AppScreens("activity_step_screen/{type}") {
        fun createRoute(type: String): String = "activity_step_screen/$type"
    }

    // 🟦 Actividades
    object ActivityCreateScreen : AppScreens("activity_create_screen")
    object ActivitiesListScreen : AppScreens("activities_list_screen")

    object ActivityDetailScreen : AppScreens("activity_detail_screen/{activityId}") {
        fun createRoute(activityId: String): String = "activity_detail_screen/$activityId"
    }

    // ❓ Preguntas
    object QuestionCreateScreen : AppScreens("question_create_screen")
    object QuestionsListScreen : AppScreens("questions_list_screen")
    object QuestionParticipateScreen : AppScreens("question_participate_screen/{questionId}") {
        fun createRoute(questionId: String) = "question_participate_screen/$questionId"
    }

    // 💬 Confianza / mensajes
    object MessageTrust : AppScreens("message_trust")

    // 🧠 Bio y registro extendido
    object UserBioScreen : AppScreens("user_bio_screen")
    object InterestSelectionScreen : AppScreens("interest_selection_screen")
    object RegistroCompletadoScreen : AppScreens("registro_completado_screen")

    // 🗺️ Mapa
    object MapPickerScreen : AppScreens("map_picker_screen")

    // 👥 Participantes y detalles de usuario
    object ParticipantsListScreen : AppScreens("participants/{activityId}") {
        fun createRoute(activityId: String) = "participants/$activityId"
    }

    object UserDetailScreen : AppScreens("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }

    // 📋 Mis participaciones
    object MyParticipationsScreen : AppScreens("my_participations")

    // 📝 Respuestas de usuarios a preguntas
    object UserAnswersScreen : AppScreens("user_answers/{questionId}") {
        fun createRoute(questionId: String) = "user_answers/$questionId"
    }

    // 🏠 Pantalla notificaciones
    object NotificationsScreen : AppScreens("notifications_screen")



}