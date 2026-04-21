import java.sql.*;
import java.util.Collection;
import java.util.Scanner;

public class MainVueltaEjercicio2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion = -1;

        // Un bucle donde manejamos los operaciones con los ciclistas
        while (opcion != 0) {
            System.out.println("----- MENU -------");
            System.out.println("1. Insertar nuevo ciclista");
            System.out.println("2. Actualizar un ciclista");
            System.out.println("3. Eliminar ciclista");

            System.out.println("0. Salir");
            System.out.println("Escribe una opcion: ");
            opcion = sc.nextInt();
            sc.nextLine();

            try (Connection connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe",
                    "RIBERA",
                    "ribera"
            )) {
                // Switch para manejar los opciones
                switch (opcion) {
                    // Caso 1: Insertar nuevo ciclista
                    // Pedimos por el teclado los datos
                    case 1:
                        System.out.println("Escribe el nombre del ciclista: ");
                        String nombre = sc.nextLine();

                        System.out.println("Escribe nacionalidad del ciclista: ");
                        String nacionalidad = sc.nextLine();

                        System.out.println("Escribe la edad del ciclista: ");
                        int edad = sc.nextInt();
                        sc.nextLine();

                        System.out.println("Escribe el id de equipo: ");
                        int idEquipo = sc.nextInt();
                        sc.nextLine();

                        // Comprobamos si el id de equipo existe o no
                        // Sino rompemos este caso
                        if (!comprobar(connection, "EQUIPO", "ID_EQUIPO", idEquipo)) {
                            System.out.println("El id de equipo no existe.");
                            break;
                        }

                        // Aqui asignamos el ID para ciclista. Si no hay ciclistas, pues el id es 1.
                        // Si hay ciclistas, se encuentra el maximo ID y le suma 1.
                        int nuevoId = 1;
                        String sqlMaxId = "SELECT MAX(ID_CICLISTA) FROM CICLISTA";

                        Statement stMaxIdCiclista = connection.createStatement();
                        ResultSet rsMaxId = stMaxIdCiclista.executeQuery(sqlMaxId);
                        if (rsMaxId.next()) {
                            nuevoId = rsMaxId.getInt(1) + 1;
                        }

                        // Sql para insertar el ciclista en tabla.
                        // PreparedStatement nos permite hacerlo.
                        String sqlInsertar = "INSERT INTO CICLISTA (ID_CICLISTA, NOMBRE, NACIONALIDAD, EDAD, ID_EQUIPO) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement psInsertar = connection.prepareStatement(sqlInsertar);
                        psInsertar.setInt(1, nuevoId);
                        psInsertar.setString(2, nombre);
                        psInsertar.setString(3, nacionalidad);
                        psInsertar.setInt(4, edad);
                        psInsertar.setInt(5, idEquipo);

                        psInsertar.executeUpdate();
                        System.out.println("Nuevo ciclista se ha insertado!");
                        break;
                    case 2:
                        // Caso 2: Actualizar el ciclista
                        // Pedimos por el teclado los datos
                        System.out.println("Escribe el id de ciclista: ");
                        int idCiclista = sc.nextInt();
                        sc.nextLine();

                        System.out.println("Escribe el edad que quieres asignar a esta ciclista: ");
                        int edadNueva = sc.nextInt();
                        sc.nextLine();

                        System.out.println("Escribe el id de equipo que quieres asignar a esta ciclista: ");
                        int idEquipoNuevo = sc.nextInt();
                        sc.nextLine();

                        // Luego comprobamos si existe ID de ciclista Y de equipo
                        if (!comprobar(connection, "CICLISTA", "ID_CICLISTA", idCiclista)) {
                            System.out.println("El id de ciclista no existe.");
                            break;
                        }

                        if (!comprobar(connection, "EQUIPO", "ID_EQUIPO", idEquipoNuevo)) {
                            System.out.println("El id de equipo no existe.");
                            break;
                        }

                        // Sql para actualizar ciclista
                        String sqlActualizar = "UPDATE CICLISTA SET EDAD = ?, ID_EQUIPO = ? WHERE ID_CICLISTA = ?";
                        PreparedStatement psActualizar = connection.prepareStatement(sqlActualizar);
                        psActualizar.setInt(1, edadNueva);
                        psActualizar.setInt(2, idEquipoNuevo);
                        psActualizar.setInt(3, idCiclista);

                        psActualizar.executeUpdate();
                        System.out.println("El ciclista se ha actualizado!");
                        break;
                    case 3:
                        // Caso 3: Eliminar ciclista
                        // Aqui solamente pedimos el id y le eliminamos utilizando un PreparedStatement con consulta
                        System.out.println("Escribe el id de ciclista: ");
                        int idCiclistaParaEliminar = sc.nextInt();
                        sc.nextLine();

                        if (!comprobar(connection, "CICLISTA", "ID_CICLISTA", idCiclistaParaEliminar)) {
                            System.out.println("El id de ciclista no existe.");
                            break;
                        }

                        String sqlParaEliminarParticipacion = "DELETE FROM PARTICIPACION WHERE ID_CICLISTA = ?";
                        PreparedStatement psEliminarParticipacion = connection.prepareStatement(sqlParaEliminarParticipacion);
                        psEliminarParticipacion.setInt(1, idCiclistaParaEliminar);
                        psEliminarParticipacion.executeUpdate();

                        String sqlEliminar = "DELETE FROM CICLISTA WHERE ID_CICLISTA = ?";
                        PreparedStatement psEliminar = connection.prepareStatement(sqlEliminar);
                        psEliminar.setInt(1, idCiclistaParaEliminar);
                        psEliminar.executeUpdate();

                        System.out.println("El ciclista se ha eliminado!");
                        break;
                    case 0:
                        System.out.println("Saliendo....");
                        break;
                    default:
                        System.out.println("Opcion no es valida!");
                        break;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Un metodo que sirve para comprobar alguna columna en alguna tabla
    public static boolean comprobar(Connection conexion, String tabla, String columna, int id) throws SQLException {
        String comprobar = "SELECT COUNT(*) FROM " + tabla + " WHERE " + columna + " = ?";
        PreparedStatement psDeComprueba = conexion.prepareStatement(comprobar);
        psDeComprueba.setInt(1, id);

        try (ResultSet rsDeComprueba = psDeComprueba.executeQuery()) {
            if (rsDeComprueba.next()) {
                int numero = rsDeComprueba.getInt(1);
                return numero > 0;
            }
        }
        return false;
    }
}
