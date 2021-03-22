package com.controledecomandas.database.dao;

import com.controledecomandas.database.PostgresConnection;
import com.controledecomandas.models.Bartable;
import com.controledecomandas.models.Order;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    public boolean insert(Order order, int workerId, int bartable_id) throws Exception {
        PostgresConnection postgresConnection = new PostgresConnection();
        boolean connected = postgresConnection.connect();

        String sqlInsertOrder = "INSERT INTO orders(bartable_id, open_at ) values(?, default);";
        String sqlInsertWorkerBartable = "INSERT INTO bartable_worker(user_id, bartable_id) VALUES(?,?)";

        try(PreparedStatement pstmtWorkerBartable = postgresConnection.createPrepedStatement(sqlInsertWorkerBartable)) {
            pstmtWorkerBartable.setInt(1, workerId);
            pstmtWorkerBartable.setInt(2, bartable_id);

            pstmtWorkerBartable.executeUpdate();

        try(PreparedStatement pstmtOrder = postgresConnection.createPrepedStatement(sqlInsertOrder)) {

            pstmtOrder.setInt(1, bartable_id);
            if(pstmtOrder.executeUpdate() == 1){
                return true;
            }


        } catch (SQLException throwables) {
            throw new Exception("Erro ao cadastrar comanda!");
        }
        } catch (SQLException throwables) {

            throw new Exception("Já existe uma comanda registrada para esta mesa!");

        } finally {
            postgresConnection.desconnect();
        }
        return false;
    }

    public List<Order> listByWorker(int workerId) throws SQLException {
        PostgresConnection postgresConnection = new PostgresConnection();
        boolean connected = postgresConnection.connect();

        String sqlQuery = "SELECT o.id, o.open_at, b.id as bartable_id, b.capacity as bartable_capacity FROM orders o " +
                "JOIN bartable b ON b.id = o.bartable_id " +
                "JOIN bartable_worker bw ON bw.bartable_id = b.id " +
                "JOIN users u ON u.id = bw.user_id " +
                "WHERE u.id = ?;";

        PreparedStatement pstmt = postgresConnection.createPrepedStatement(sqlQuery);

            pstmt.setInt(1,workerId);
            ResultSet rs = pstmt.executeQuery();
            List<Order> orders = new ArrayList<>();

            while (rs.next()) {
                        Order order = new Order();
                        order.setId(rs.getInt("id"));
                        order.setOpenAt(rs.getTimestamp("open_at"));
                        Bartable bartable = new Bartable();
                        bartable.setId(rs.getInt("bartable_id"));
                        bartable.setCapacity(rs.getInt("bartable_capacity"));
                        order.setBartable(bartable);

                        orders.add(order);
                    }

        postgresConnection.desconnect();
        return orders;
    }

}