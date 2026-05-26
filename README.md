# Proyecto Final: Pizzería Microservicios

Este repositorio contiene la arquitectura de microservicios desarrollada para la gestión de una pizzería.

## Equipo de Desarrollo
- **Emilia:** Microservicios de `autenticacion`, `carrito`, `pedido` y `certificacion`.
- **Nicolas:** Microservicios de `catalogo`, `inventario` y `reseñas`.
- **Matias:** Microservicios de `pagos`, `reparto` y `notificaciones`.

## Estructura del Proyecto
Cada carpeta representa un microservicio independiente desarrollado con Spring Boot:
- `autenticacion`: Gestión de usuarios y seguridad.
- `carrito`: Manejo de productos seleccionados por el cliente.
- `pedido`: Gestión de órdenes de compra.
- `certificacion`: Validación de entregas y puntualidad.
- `reparto`: Gestión de la logística de envío y asignación de repartidores.
- `inventario`: Control de stock de productos.
- `catalogo`: Gestión de productos, precios y descripción del menú.
- `resenas`: Sistema de calificación y comentarios de los clientes.
- `notificaciones`: Envío de alertas y actualizaciones sobre el estado del pedido.
- `pago`: Procesamiento de transacciones y métodos de pago.

## Funcionalidades Implementadas

- **Autenticación:** Gestión segura de usuarios y roles mediante JWT.

- **Catálogo e Inventario:** Consulta de productos en tiempo real con control de stock.

- **Gestión de Pedidos:** Flujo completo desde la selección del carrito hasta el pago y confirmación.

- **Logística:** Asignación automática de repartidores y seguimiento de entregas.

- **Sistema de Reseñas:** Retroalimentación de clientes sobre productos y servicios.

## Pasos para ejecutar el proyecto

1. **Clonar el repositorio:** `git clone https://github.com/Emiiiiiiiiiiiiiii/Proyecto-Pizzeria-Final`

2. **Base de Datos:** Asegúrate de tener configurada tu instancia de base de datos local o en la nube según el archivo `application.properties`.

3. **Ejecutar microservicios:**

   - Navegar a cada carpeta (ej. `autenticacion`).

   - Ejecutar el comando: `./mvnw spring-boot:run` (o usar el IDE de tu preferencia).

4. **Pruebas:** Utilizar Postman para realizar peticiones a los puertos definidos en cada microservicio.


---
*Nota: Los microservicios han sido configurados para comunicarse mediante RestTemplate.*
