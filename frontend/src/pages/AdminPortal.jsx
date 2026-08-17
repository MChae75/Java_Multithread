import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Trash2, RefreshCw, LayoutDashboard } from 'lucide-react';

const API_URL = 'http://localhost:8081/api';

const AdminPortal = () => {
  const [orders, setOrders] = useState([]);
  const [products, setProducts] = useState([]);

  const fetchData = async () => {
    try {
      const [ordersRes, prodRes] = await Promise.all([
        axios.get(`${API_URL}/orders`),
        axios.get(`${API_URL}/products`)
      ]);
      setOrders(ordersRes.data);
      setProducts(prodRes.data);
    } catch (error) {
      console.error('Failed to fetch admin data:', error);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const deleteOrder = async (orderId) => {
    try {
      await axios.delete(`${API_URL}/orders/${orderId}`);
      fetchData();
    } catch (error) {
      console.error('Failed to delete order:', error);
    }
  };

  return (
    <div className="container animate-fade-in">
      <h1><LayoutDashboard size={40} style={{ verticalAlign: 'bottom', marginRight: '1rem' }} />Admin Dashboard</h1>
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        <div className="glass-panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2>Live Inventory</h2>
            <button className="btn btn-secondary" onClick={fetchData}><RefreshCw size={16} /> Refresh</button>
          </div>
          <table className="admin-table">
            <thead>
              <tr>
                <th>Product ID</th>
                <th>Name</th>
                <th>Available Stock</th>
              </tr>
            </thead>
            <tbody>
              {products.map(product => (
                <tr key={product.id}>
                  <td>{product.id}</td>
                  <td><strong>{product.name}</strong></td>
                  <td><span className={`stock-badge ${product.stock < 10 ? 'low' : ''}`}>{product.stock}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="glass-panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2>Recent Orders</h2>
          </div>
          <table className="admin-table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>User ID</th>
                <th>Product</th>
                <th>Qty</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 && (
                <tr><td colSpan="5" style={{ textAlign: 'center' }}>No orders found</td></tr>
              )}
              {orders.map(order => (
                <tr key={order.orderId}>
                  <td>{order.orderId.substring(0, 8)}...</td>
                  <td>{order.userId}</td>
                  <td>{order.productId}</td>
                  <td>{order.quantity}</td>
                  <td>
                    <button className="btn btn-danger" style={{ padding: '0.4rem 0.8rem' }} onClick={() => deleteOrder(order.orderId)}>
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminPortal;
