package com.trigon.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trigon.entity.Address;
import com.trigon.entity.Cart;
import com.trigon.entity.CartItem;
import com.trigon.entity.Order;
import com.trigon.entity.OrderItem;
import com.trigon.entity.PaymentDetails;
import com.trigon.entity.User;
import com.trigon.exception.OrderException;
import com.trigon.repository.AddressRepository;
import com.trigon.repository.CartRepository;
import com.trigon.repository.OrderItemRepository;
import com.trigon.repository.OrderRepository;
import com.trigon.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService{


	
	@Autowired
	private OrderRepository orderRepo;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private OrderItemService orderItemService;
	
	@Autowired
	private OrderItemRepository orderItemRepo;

	
	@Override
	public Order findOrderById(Long id) throws OrderException {
		Optional<Order> opt=orderRepo.findById(id);
		if(opt.isPresent()) {
			return opt.get();
		}
		throw new OrderException("Order not exist with id ::"+id);
	}

	@Override
	public Order createOrder(User user, Address shippingAddress) {
		shippingAddress.setUser(user);
		Address address=addressRepo.save(shippingAddress);
		user.getAddress().add(address);
		userRepo.save(user);
		
		Cart cart=cartService.findUserCart(user.getId());
		List<OrderItem> orderItems=new ArrayList<>();
		
		for(CartItem item:cart.getCartItem()) {
			OrderItem orderItem=new OrderItem();
			
			orderItem.setPrice(item.getPrice());
			orderItem.setProduct(item.getProduct());
			orderItem.setQuantity(item.getQuantity());
			orderItem.setSize(item.getSize());
			orderItem.setDiscountedPrice(item.getDiscountedPrice());
			orderItem.setUserId(item.getUserId());
			
			 OrderItem createdOrderItem=orderItemRepo.save(orderItem);
			 orderItems.add(createdOrderItem);
		}
		
		Order createdOrder=new Order();
		createdOrder.setUser(user);
		createdOrder.setOrderItem(orderItems);
		createdOrder.setTotalPrice(cart.getTotalPrice());
		createdOrder.setTotalDiscountedPrice(cart.getTotalDiscountedPrice());
		createdOrder.setDiscount(cart.getDiscounte());
		createdOrder.setTotalItem(cart.getTotalItem());
		createdOrder.setShippingAddress(address);
		createdOrder.setOrderDate(LocalDateTime.now());
		createdOrder.setOrderStatus("PENDING");
		createdOrder.getPaymentDetails().setPaymentStatus("PENDING");
		createdOrder.setCreatedAt(LocalDateTime.now());
		
		// ✅ IMPORTANT FIX
	    PaymentDetails paymentDetails = new PaymentDetails();
	    paymentDetails.setPaymentStatus("PENDING");
	    paymentDetails.setPaymentMethod("NOT_SELECTED");
	    createdOrder.setPaymentDetails(paymentDetails);
		Order savedOrder=orderRepo.save(createdOrder);
		System.out.println("Order that i have saved is :: "+savedOrder);
		
		for(OrderItem item:orderItems) {
			item.setOrder(savedOrder);
			orderItemRepo.save(item);
		}
		return savedOrder;
	}

	@Override
	public List<Order> usersOrderHistory(Long userId) {
		List<Order> orders=orderRepo.getUsersOrders(userId);
		return orders;
	}

	@Override
	public Order confirmOrder(Long orderId) throws OrderException {
		Order order=findOrderById(orderId);
		order.setOrderStatus("CONFIRMED");
		return orderRepo.save(order);
	}

	@Override
	public Order placeOrder(Long orderId) throws OrderException {
		Order order=findOrderById(orderId);
		order.setOrderStatus("PLACED");
		order.getPaymentDetails().setPaymentStatus("COMPLETED");
		return order;
	}

	@Override
	public Order shippedOrder(Long orderId) throws OrderException {
		Order order=findOrderById(orderId);
		order.setOrderStatus("SHIPPED");
		return orderRepo.save(order);
	}

	@Override
	public Order deliveredOrder(Long orderId) throws OrderException {
		Order order=findOrderById(orderId);
		order.setOrderStatus("DELIVERED");
		return orderRepo.save(order);
	}

	@Override
	public Order canceledOrder(Long orderId) throws OrderException {
		Order order=findOrderById(orderId);
		order.setOrderStatus("CANCELLED");
		return orderRepo.save(order);	}

	@Override
	public List<Order> getAllOrder() {
		// TODO Auto-generated method stub
		return orderRepo.findAll();
	}

	@Override
	public void deleteOrder(Long orderId) throws OrderException {
		orderRepo.deleteById(orderId);
		
	}

}
