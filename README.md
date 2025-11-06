🎓 UniConnect

UniConnect es una aplicación móvil inteligente que conecta estudiantes universitarios dentro de una misma institución, permitiendo crear comunidad, compartir eventos, chatear, registrarse a actividades y generar espacios de interacción social.
Su objetivo es fortalecer el sentido de comunidad en el campus mediante herramientas digitales modernas.

🚀 Características principales

👤 Autenticación segura mediante Firebase Authentication

💬 Chat en tiempo real entre estudiantes

📸 Registro facial y carga de foto de perfil

🗓️ Explorador de actividades y eventos universitarios

🎭 Pantalla de perfil personal editable

🌗 Interfaz moderna con Jetpack Compose (Material Design 3)

☁️ Almacenamiento en la nube con Firebase Storage

🗄️ Base de datos en tiempo real con Firestore

🛠️ Tecnologías utilizadas
🔹 Mobile App (Android)
Tecnología	Uso
Kotlin	Lenguaje principal
Jetpack Compose	Interfaz moderna declarativa
Firebase Authentication	Inicio de sesión y registro
Cloud Firestore	Base de datos de usuarios, chats y eventos
Firebase Storage	Imágenes de perfil y carga multimedia
ViewModel + StateFlow	Manejo de estado y lógica de UI
📂 Estructura del proyecto
UniConnect/
│
├── app/
│   ├── src/main/java/com/utadeo/uniconnect/
│   │   ├── data/
│   │   │   ├── model/           # Modelos de datos
│   │   │   ├── repository/      # Firebase Repositories
│   │   │   └── manager/         # Controladores de registro
│   │   ├── ui/
│   │   │   ├── screens/         # Pantallas Jetpack Compose
│   │   │   └── theme/           # Colores, tipografías y estilos
│   │   ├── UniConnectApp.kt     # Setup de navegación
│   │   └── MainActivity.kt      # Punto de entrada
│   └── build.gradle.kts
│
├── build.gradle.kts
└── settings.gradle.kts

⚙️ Instalación y ejecución

1️⃣ Clona el repositorio

git clone https://github.com/Felipe7774/Uniconet.git
cd Uniconet/UniConnect


2️⃣ Abre el proyecto en Android Studio

Recomendado: Android Studio Hedgehog o superior

3️⃣ Sincroniza dependencias
Android Studio lo hará automáticamente o puedes forzar:

File > Sync Project with Gradle Files


4️⃣ Configura Firebase

Crea un proyecto en Firebase Console

Descarga el archivo google-services.json

Colócalo en:

app/src/main/


5️⃣ Ejecuta la app en tu teléfono o emulador

🔗 Servicios en la nube utilizados
Servicio	Función
Firebase Auth	Registro e ingreso
Cloud Firestore	Chats, perfiles y actividades
Firebase Storage	Imágenes de usuario
🎯 Próximas mejoras (Roadmap)

🗺️ Geolocalización de eventos del campus

🔔 Notificaciones push

👥 Grupos temáticos y foros estudiantiles
