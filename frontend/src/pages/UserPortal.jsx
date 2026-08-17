import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ShoppingCart, CheckCircle, AlertTriangle } from 'lucide-react';

const API_URL = 'http://localhost:8081/api';

const UserPortal = () => {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState({});
  const [orderStatus, setOrderStatus] = useState(null);

  useEffect(() => {
    fetchInventory();
  }, []);

  const fetchInventory = async () => {
    try {
      const response = await axios.get(`${API_URL}/products`);
      setProducts(response.data);
    } catch (error) {
      console.error('Failed to fetch products:', error);
    }
  };

  const addToCart = (productId) => {
    setCart(prev => ({
      ...prev,
      [productId]: (prev[productId] || 0) + 1
    }));
  };

  const placeOrder = async () => {
    setOrderStatus(null);
    try {
      // For simplicity, place an order for the first item in the cart
      const productId = Object.keys(cart)[0];
      if (!productId) return;
      
      const quantity = cart[productId];

      const response = await axios.post(`${API_URL}/orders`, {
        userId: 'user-' + Math.floor(Math.random() * 1000),
        productId: productId,
        quantity: quantity
      });

      setOrderStatus({ type: 'success', message: 'Order placed successfully! Status: ' + response.data.status });
      setCart({});
      fetchInventory();
    } catch (error) {
      setOrderStatus({ 
        type: 'error', 
        message: error.response?.data?.message || 'Failed to place order. Might be out of stock!' 
      });
    }
  };

  const dailyDeal = products[0];
  const otherDeals = products.slice(1);

  return (
    <div className="container">
      {orderStatus && (
        <div className={`alert ${orderStatus.type === 'success' ? 'alert-success' : 'alert-danger'}`}>
          {orderStatus.type === 'success' ? <CheckCircle /> : <AlertTriangle />}
          <span>{orderStatus.message}</span>
        </div>
      )}

      {dailyDeal && (
        <div className="daily-deal">
          <div className="daily-deal-image">
            📦
          </div>
          <div className="daily-deal-info">
            <h2 className="daily-deal-title">{dailyDeal.name}</h2>
            <p style={{ marginBottom: '1rem', color: 'var(--text-muted)' }}>{dailyDeal.description}</p>
            <div className="price-block">
              <span style={{ textDecoration: 'line-through', color: 'var(--text-muted)', fontSize: '1.2rem', marginRight: '1rem' }}>
                ${dailyDeal.originalPrice}
              </span>
              <span className="price">${dailyDeal.price}</span>
            </div>
            
            <div style={{ marginBottom: '2rem' }}>
              <span className={`stock-badge ${dailyDeal.stock === 0 ? 'sold-out' : dailyDeal.stock < 20 ? 'low' : ''}`}>
                {dailyDeal.stock === 0 ? 'SOLD OUT!' : `${dailyDeal.stock} LEFT`}
              </span>
            </div>

            <button 
              className="btn" 
              onClick={() => addToCart(dailyDeal.id)}
              disabled={dailyDeal.stock === 0}
              style={{ width: '100%', maxWidth: '300px', opacity: dailyDeal.stock === 0 ? 0.5 : 1 }}
            >
              {dailyDeal.stock === 0 ? 'TOO LATE' : 'I WANT ONE'}
            </button>
          </div>
        </div>
      )}

      <h3 style={{ marginBottom: '1.5rem', color: 'var(--woot-dark)', borderBottom: '2px solid var(--woot-border)', paddingBottom: '0.5rem' }}>
        More Stuff You Don't Need
      </h3>

      <div className="product-grid">
        {otherDeals.map(product => (
          <div key={product.id} className="deal-card">
            <div style={{ height: '200px', backgroundColor: 'var(--woot-light-gray)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '4rem', borderBottom: '1px solid var(--woot-border)' }}>
              👕
            </div>
            <div style={{ padding: '1.5rem', flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
              <h4 style={{ fontSize: '1.2rem', marginBottom: '0.5rem' }}>{product.name}</h4>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', flexGrow: 1, marginBottom: '1rem' }}>{product.description}</p>
              <div style={{ fontSize: '1.5rem', fontWeight: 900, fontFamily: 'Montserrat, sans-serif', color: 'var(--woot-dark)', marginBottom: '1rem' }}>
                ${product.price}
              </div>
              <button 
                className="btn"
                style={{ width: '100%', padding: '0.8rem', fontSize: '1rem' }}
                onClick={() => addToCart(product.id)}
                disabled={product.stock === 0}
              >
                {product.stock === 0 ? 'GONE' : 'ADD TO CART'}
              </button>
            </div>
          </div>
        ))}
      </div>

      {Object.keys(cart).length > 0 && (
        <div className="cart-summary">
          <h3><ShoppingCart size={24} style={{ verticalAlign: 'middle', marginRight: '0.5rem' }} /> CART</h3>
          <ul style={{ listStyle: 'none', margin: '1rem 0', fontFamily: 'Montserrat, sans-serif', fontWeight: 700 }}>
            {Object.entries(cart).map(([id, qty]) => (
              <li key={id} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', borderBottom: '1px solid var(--woot-border)', paddingBottom: '0.5rem' }}>
                <span>{id}</span>
                <span>QTY: {qty}</span>
              </li>
            ))}
          </ul>
          <button className="btn" style={{ width: '100%', justifyContent: 'center' }} onClick={placeOrder}>
            CHECKOUT NOW
          </button>
        </div>
      )}
    </div>
  );
};

export default UserPortal;
