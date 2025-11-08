package com.utadeo.uniconnect.data.model.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.utadeo.uniconnect.data.model.activity.*
import com.utadeo.uniconnect.data.model.ui.Login.*
import com.utadeo.uniconnect.data.model.ui.Login.Register.InterestSelectionScreen
import com.utadeo.uniconnect.data.model.ui.Login.Register.MessageTrustScreen
import com.utadeo.uniconnect.data.model.ui.Login.Register.RegistroCompletadoScreen
import com.utadeo.uniconnect.data.model.ui.Login.Register.UserBioScreen
import com.utadeo.uniconnect.data.model.ui.Register.*
import com.utadeo.uniconnect.data.model.ui.activity.*
import com.utadeo.uniconnect.ui.ChatScreen
import com.utadeo.uniconnect.ui.splash.SplashScreen
import com.utadeo.uniconnect.data.model.User.UserProfileScreen
import androidx.navigation.navArgument
import com.utadeo.uniconnect.data.model.chat.IndividualChatScreen
import com.utadeo.uniconnect.data.model.chat.ChatsListScreen
import com.utadeo.uniconnect.data.model.ui.Login.notifications.NotificationsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreens.SplashScreen.route
    ) {
        // 🟢 Pantalla inicial (Splash)
        composable(AppScreens.SplashScreen.route) {
            SplashScreen(navController)
        }

        // 🟠 Pantalla de inicio (Login o registro)
        composable(AppScreens.LoginScreen.route) {
            LoginAndRegister(navController)
        }

        composable(route = "chats_list") {
            ChatsListScreen(navController = navController)
        }

        composable(
            route = AppScreens.IndividualChatScreen.route + "/{chatId}/{otherUserId}/{otherUserName}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType },
                navArgument("otherUserName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            IndividualChatScreen(
                navController = navController,
                chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: "",
                otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""
            )
        }


        // 🟣 Pantalla de registro
        composable(AppScreens.RegisterScreen.route) {
            RegisterScreen(navController)
        }

        // 🔐 Registro con contraseña
        composable(
            route = AppScreens.RegisterWithPasswordScreen.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RegisterWithPasswordScreen(navController, email)
        }

        // 🔑 Login con contraseña
        composable(AppScreens.LoginWithPasswordScreen.route) {
            LoginWithPassword(navController)
        }

        // 🏠 Pantalla principal
        composable(AppScreens.HomeScreen.route) {
            HomeScreen(navController)
        }

        // 💬 Pantalla del chat (con argumento chatId)
        composable(
            route = AppScreens.ChatScreen.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(navController, chatId)
        }

        // ⚙️ Pantalla de configuración
        composable(AppScreens.SettingsScreen.route) {
            SettingsScreen(navController)
        }

        // 📸 Pantalla de foto de perfil
        composable(AppScreens.ProfilePictureScreen.route) {
            ProfilePictureScreen(navController)
        }

        // 🪪 Pantalla de verificación de identidad
        composable(AppScreens.IdentityVerification.route) {
            IdentityVerification(navController)
        }

        // 🟡 Flujo de actividades
        composable(AppScreens.ActivityFlowScreen.route) {
            ActivityFlowScreen(navController)
        }

        // 🟦 Pantalla para crear pregunta
        composable(AppScreens.QuestionCreateScreen.route) {
            QuestionCreateScreen(navController)
        }

        // 📋 Lista de preguntas
        composable(AppScreens.QuestionsListScreen.route) {
            QuestionsListScreen(navController)
        }

        // 🟦 Pantalla para participar en una pregunta específica
        composable(
            route = AppScreens.QuestionParticipateScreen.route,
            arguments = listOf(navArgument("questionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            QuestionParticipateScreen(navController, questionId)
        }

        // 💬 Mensaje de confianza
        composable(AppScreens.MessageTrust.route) {
            MessageTrustScreen(navController)
        }

        // 📝 Creación de perfil
        composable(AppScreens.ProfileCreationScreen.route) {
            ProfileCreationScreen(navController)
        }

        // 📝 Biografía de usuario
        composable(AppScreens.UserBioScreen.route) {
            UserBioScreen(navController)
        }

        // 🎯 Selección de intereses
        composable(AppScreens.InterestSelectionScreen.route) {
            InterestSelectionScreen(navController)
        }

        // ✅ Registro completado
        composable(AppScreens.RegistroCompletadoScreen.route) {
            RegistroCompletadoScreen(navController)
        }

        // 🎨 Crear actividad
        composable(AppScreens.ActivityCreateScreen.route) {
            ActivityCreateScreen(navController)
        }

        // 🗺️ Selector de mapa
        composable(AppScreens.MapPickerScreen.route) {
            MapPickerScreen(navController = navController)
        }

        // 📋 Lista de actividades
        composable(AppScreens.ActivitiesListScreen.route) {
            ActivitiesListScreen(navController)
        }

        // 🔍 Detalle de actividad - IMPORTANTE
        composable(
            route = AppScreens.ActivityDetailScreen.route,
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
            ActivityDetailScreen(navController, activityId)
        }

        // 👤 Perfil de Usuario sin parámetro (mi perfil)
        composable(AppScreens.UserProfileScreen.route.replace("/{userId}", "")) {
            UserProfileScreen(navController, "me")
        }

        // 👤 Perfil de Usuario con parámetro
        composable(
            route = AppScreens.UserProfileScreen.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "me"
            UserProfileScreen(navController, userId)
        }

        // 👥 Lista de participantes
        composable(
            route = AppScreens.ParticipantsListScreen.route,
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
            ParticipantsListScreen(navController = navController, activityId = activityId)
        }

        // 👤 Detalle de usuario
        composable(
            route = AppScreens.UserDetailScreen.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(navController = navController, userId = userId)
        }

        // 📋 Mis participaciones
        composable(AppScreens.MyParticipationsScreen.route) {
            MyParticipationsScreen(navController)
        }

        // 📝 Respuestas de preguntas
        composable(
            route = AppScreens.UserAnswersScreen.route,
            arguments = listOf(navArgument("questionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            UserAnswersScreen(navController, questionId)
        }
        // 🔔 NOTIFICACIONES
        composable(route = AppScreens.NotificationsScreen.route) {
            NotificationsScreen(navController)
        }

    }
}