# Proyecto Final: Pizzería Microservicios

Este repositorio contiene la arquitectura de microservicios desarrollada para la gestión de una pizzería.

## Equipo de Desarrollo
- **Emilia:** Microservicios de `autenticacion`, `carrito`, `pedido` y `certificacion`.
- **Nicolas:** Microservicios de `catalogo`, `inventario` y `reseñas`.
- **Matias:** Microservicios de `pagos`, `reparto` y `notificaciones`.

## 📁 Estructura del Proyecto
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
---
*Nota: Los microservicios han sido configurados para comunicarse mediante RestTemplate.*
