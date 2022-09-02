# Tourapp

Aplicación Android para turismo en la Comunidad de Madrid.

## Objetivo

El objetivo de esta aplicación ha sido desarrollar una aplicación móvil para Android que permita al usuario compartir con otras personas los lugares que considere interesantes pero que no tengan que ser necesariamente famosos o aparecer en la guías turísticas. De esta forma se puede proponer una forma de turismo
diferente.

## Características de la aplicación

- [Uso de cuenta (Login)](#cuenta).
- [Menú lateral](#menu).
- [Perfil de lugares de interés](#perfil).
- [Localización lugar](#localizacion).
- [Listas de lugares](#lista).
- [Dar alta lugar](#alta).
- [Comentarios](#comentarios).

### Cuenta aplicación<a name="cuenta"></a>

El usuario para poder acceder a todas la funcionalidades de la aplicación requiere hacerse una cuenta en la aplicación de Tourapp, aunque es posible acceder a los servicios de forma parcial sin la misma. 

También si lo desea puede dar de baja su cuenta.

<em>* El usuario sin cuenta tendrá ver la capacidad de filtrar las listas de lugares en función a la categoría de los mismos y su puntuación y ver la
información disponible de los lugares, pero no le será posible hacer uso de las demás funcionalidades que se mencionarán a continuación.</em>

<p float="left">
  <img src="images/login2.png" alt="pantalla login" width="310" style="object-fit: cover;"/>
  <img src="images/registro1.png" alt="pantalla signup" width="310" style="object-fit: cover;"/>
</p>

### Menú lateral<a name="menu"></a>

Desde la pantalla principal de la aplicación se puede desplegar un menú lateral desde el cual es posible acceder al resto de las opciones y servicios que ofrece la aplicación.

<p float="left">
  <img src="images/userdata1.png" alt="menú sin desplegar" width="310" style="object-fit: cover;"/>
  <img src="images/menulateral1.png" alt="menú desplegado" width="310" style="object-fit: cover;"/>
</p>

### Perfil de lugares<a name="perfil"></a>

Se muestra la información de los lugares dados de alta en la aplicación (imágenes, información, localización...). También es posible añadir a un lugar a la lista de favoritos y ver sus comentarios y localización.

<p float="left">
  <img src="images/lugarinfo1.png" alt="pantalla datos lugar 1" width="310" style="object-fit: cover;"/>
  <img src="images/lugarinfo2.png" alt="pantalla datos lugar 2" width="310" style="object-fit: cover;"/>
</p>

#### Localización del lugar<a name="localizacion"></a>

En el perfil del lugar puede aparecer información sobre la localización del mismo.

<p float="left">
  <img src="images/lugarinfo2.png" alt="pantalla datos lugar" width="310" style="object-fit: cover;"/>
  <img src="images/capturamapa.png" alt="mapa con localización lugar" width="310" style="object-fit: cover;"/>
</p>

### Listas de lugares<a name="lista"></a>

Los distintos lugares se muestran al usuario en formato de listas, las cuales se pueden filtrar por distintos criterios (categorías, puntuación del lugar, preferencias del usuario...).
Las categorías son: museos, parques, lugares históricos, restaurantes, etc.

<p float="left">
  <img src="images/listalug1.png" alt="pantalla lista lugares" width="290" style="object-fit: cover;"/>
  <img src="images/listalug2.png" alt="pantalla lista lugares con filtro categorías" width="290" style="object-fit: cover;"/>
  <img src="images/listalug4.png" alt="pantalla lista lugar con otros filtros" width="290" style="object-fit: cover;"/>
</p>

#### Listas personales<a name="listap"></a>

Es posible para cada usuario crear, editar o eliminar sus propias listas personalizadas.

<p float="left">
  <img src="images/userlists1.png" alt="pantalla listas del usuario" width="290" style="object-fit: cover;"/>
  <img src="images/userlists2.png" alt="listas usuario opciones" width="290" style="object-fit: cover;"/>
  <img src="images/userlists3.png" alt="edición de nombre de lista usuario" width="290" style="object-fit: cover;"/>
</p>

### Dar alta lugar<a name="alta"></a>

En la lista de lugares hay una opción para agregar o dar de alta un lugar. Una vez en la pantalla se agregan todos los datos necesarios (nombre, descripción, localización, etiquetas/categorías ...). Y este se da de alta en la base de datos.

<p float="left">
  <img src="images/listalug52.png" alt="pantalla lista lugares" width="310" style="object-fit: cover;"/>
  <img src="images/altalug1.png" alt="pantalla añadir lugar 1" width="310" style="object-fit: cover;"/>
  <img src="images/altalug4.png" alt="pantalla añadir lugar 4" width="310" style="object-fit: cover;"/>
  <img src="images/altalug2.png" alt="pantalla añadir lugar 2" width="310" style="object-fit: cover;"/>
  <img src="images/altalug32.png" alt="pantalla añadir lugar 3" width="310" style="object-fit: cover;"/>
</p>


### Comentarios de usuarios<a name="comentarios"></a>

El perfil de cada lugar tiene una sección de comentarios para que cada usuario pueda compartir su opinión con los demás.

<p float="left">
  <img src="images/comen2.png" alt="acceso sección comentarios" width="290" style="object-fit: cover;"/>
  <img src="images/comen1.png" alt="pantalla de comentarios" width="290" style="object-fit: cover;"/>
</p>

## Especificaciones aplicación<a name="especificaciones"></a>

La aplicación puede funcionar en cualquier smartphone o tablet que tenga Android 7.x o superior además de requerir conexión a internet.

### Instalación apk<a name="apk"></a>

Debido a que la aplicación no se encuentra en ninguna tienda la única forma de instalarla es generar la <em>.apk</em> utilizando el software Android Studio, y que el smartphone o tablet en cuestión permita instalar apk individuales.

Adjunto un <a href="https://www.lifewire.com/install-apk-on-android-4177185" target="_blank">enlace</a> para mostrar como se hace.






