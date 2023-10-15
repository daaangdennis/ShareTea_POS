package entities;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.text.DecimalFormat;


public class sales {
    
    public static ArrayList<ArrayList<Object>> ProductSales(Connection conn, String startDate, String endDate){
        ArrayList<ArrayList<Object>> productSaleList = new ArrayList<>();
        ArrayList<Object> nameList = new ArrayList<>();
        ArrayList<Object> soldList = new ArrayList<>();
        productSaleList.add(nameList);
        productSaleList.add(soldList);

        try {
            String query = "SELECT (SELECT name FROM product WHERE product_id = sub.product_id) AS product_name, SUM(quantity) AS total_quantity FROM order_product sub WHERE order_id IN (SELECT order_id FROM orders WHERE order_date >= ? AND order_date <= ?) GROUP BY product_id ORDER BY product_id";
            PreparedStatement preparedStatement = conn.prepareStatement(query);

            preparedStatement.setDate(1, Date.valueOf(startDate));
            preparedStatement.setDate(2, Date.valueOf(endDate));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("product_name");
                Integer sold = resultSet.getInt("total_quantity");
                productSaleList.get(0).add(name);
                productSaleList.get(1).add(sold);
            }
            return productSaleList;
            
        } catch (Exception e) {
            System.out.println("Couldn't display product sales.");
        }
        return productSaleList;
    }

    public static ArrayList<ArrayList<Object>> lowStock(Connection conn){
        ArrayList<ArrayList<Object>> lowStockList = new ArrayList<>();
        ArrayList<Object> nameList = new ArrayList<>();
        ArrayList<Object> lowList = new ArrayList<>();
        lowStockList.add(nameList);
        lowStockList.add(lowList);

        try {
            String query = "SELECT (SELECT name FROM inventory WHERE inventory_id = inv.inventory_id), quantity AS remaining FROM inventory inv WHERE quantity < 25";
            PreparedStatement preparedStatement = conn.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                Integer num = resultSet.getInt("remaining");
                lowStockList.get(0).add(name);
                lowStockList.get(1).add(num);
            }
            return lowStockList;
            
        } catch (Exception e) {
            System.out.println("Couldn't display low stock.");
        }
        return lowStockList;
    }

    public static ArrayList<ArrayList<Object>> excessStock(Connection conn, String startDate){
        ArrayList<ArrayList<Object>> inventoryUsage = new ArrayList<>();
        ArrayList<Object> nameList = new ArrayList<>();
        ArrayList<Object> usedList = new ArrayList<>();
        inventoryUsage.add(nameList);
        inventoryUsage.add(usedList);

        try {
            String query = "SELECT (SELECT i.name FROM inventory i WHERE i.inventory_id = ip.inventory_id) AS inventory_name,  SUM((SELECT COALESCE(SUM(op.quantity),0) FROM order_product op WHERE op.product_id = ip.product_id AND op.order_id IN(SELECT o.order_id FROM orders o WHERE o.order_date >= ?))) AS quantity_used, (SELECT COALESCE(SUM(i.quantity),0) FROM inventory i WHERE i.inventory_id = ip.inventory_id) AS current_quantity FROM inventory_product ip GROUP BY ip.inventory_id, inventory_name";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setDate(1, Date.valueOf(startDate));

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("inventory_name");
                Double used = resultSet.getInt("quantity_used") * 1.00;
                Double current = resultSet.getInt("current_quantity") * 1.00;
                Double percentUsed =  used / (current+used);     
                percentUsed = Math.floor(percentUsed * 100) / 100;

                inventoryUsage.get(0).add(name);
                inventoryUsage.get(1).add(percentUsed);
            }
            return inventoryUsage;
            
        } catch (Exception e) {
            System.out.println("Couldn't display excess stock.");
        }
        return inventoryUsage;
    }
}