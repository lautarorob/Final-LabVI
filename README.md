El proyecto esta basado en las aplicaciones/reproductores de musica Spotify y Deezer

Requerimientos Funcionales (RF)
1. Gestión de Sesión y Perfil con Room
RF01: El sistema debe permitir el registro e inicio de sesión de usuarios de forma local, persistiendo las credenciales y datos del perfil (nombre, correo y contraseña) en una base de datos local utilizando room.
RF02: El sistema debe realizar validaciones estrictas en los formularios de registro (ej. formato de correo electrónico válido, campos no vacíos y longitud mínima de contraseña) mostrando mensajes de error claros en la interfaz de usuario en caso de fallos.
RF03 El sistema debe guardar mediante Room información sobre las listas del usuario
2. Geolocalización y Recomendaciones Regionales (APIs de geolocal.)
RF04: El sistema debe mostrar una sección de recomendaciones regionales con contenido musical adaptado a la ubicación geográfica actual del usuario.
RF05: La sección debe contar con la posibilidad de encender la ubicación desde la aplicación 
3. Conectividad y Consumo de APIs de música
RF06: El sistema debe realizar un check de conectividad a internet antes de realizar cualquier petición a la API de música. Si no hay conexión, debe desplegar un mensaje de error o pantalla advirtiendo al usuario.
RF07: El sistema debe conectarse a una API externa de música para buscar y recuperar canciones, artistas y portadas de álbumes en tiempo real.
4. Funciones Básicas para el usuario
RF08: El sistema debe permitir reproducir el flujo de audio de las canciones obtenidas de la API, ofreciendo los controles básicos de reproducción (Play/Pause) en un mini-reproductor integrado.
RF09: El sistema debe incluir una sección de reproducción donde se visualicen controles básicos de reproducción, nombre, portada y artista de la canción.
RF10: La app debe permitir crear listas de música personalizada, mediante los “Me gusta”.
RF11: Debe permitir la exploración/búsqueda de canciones.
RF12: Debe contar con una vista donde se agrupen las listas del usuario.


Requerimientos No Funcionales (RNF)
1. Arquitectura de Software (MVVM)
RNF01 (Patrón Arquitectónico): La aplicación debe estar estructurada bajo el patrón de arquitectura MVVM (Model-View-ViewModel), asegurando la separación de la lógica de presentación, la lógica de negocio y el acceso a datos.
2. Gestión de Datos Local y Remota
RNF02 (Persistencia Local): El almacenamiento local del usuario y sus preferencias de sesión debe gestionarse mediante Room.
RNF03 (Consumo Asíncrono): El consumo de las APIs de música debe realizarse en hilos secundarios utilizando librerías como Retrofit, garantizando que las peticiones pesadas no bloqueen el hilo principal de la interfaz (UI Thread).
3. Robustez y Experiencia de Usuario (UX)
RNF05 (Tratamiento de Errores): Toda la aplicación debe implementar una política centralizada de captura de excepciones y validación de conectividad, garantizando la resiliencia del sistema ante fallos de hardware o de red sin degradar la experiencia del usuario.


Mockups de la app: https://stitch.withgoogle.com/projects/14953757390858587628

API usada: https://developers.deezer.com/api
