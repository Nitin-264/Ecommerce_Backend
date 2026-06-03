package com.trigon.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.trigon.authresponse.ApiResponse;
import com.trigon.entity.Order;
import com.trigon.entity.PaymentLinkResponse;
import com.trigon.exception.OrderException;
import com.trigon.repository.OrderRepository;
import com.trigon.service.OrderService;
import com.trigon.service.UserService;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final AuthController authController;

	@Value("${razorpay.api.key}")
	String apiKey;
	
	@Value("${razorpay.api.secret}")
	String secretKey;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	OrderRepository orderRepo;

    PaymentController(AuthController authController) {
        this.authController = authController;
    }
	
	@PostMapping("/payments/{orderId}")
	public ResponseEntity<PaymentLinkResponse> createPaymentLink(@PathVariable Long orderId,@RequestHeader("Authorization") String jwt) throws OrderException, RazorpayException{
		Order order=orderService.findOrderById(orderId);
		System.out.println("OrderId for confirm the payment is :: "+orderId);
		System.out.println("API key is :: "+apiKey);
		System.out.println("Secret key is :: "+secretKey);
		
		try {
			RazorpayClient razorpay=new RazorpayClient(apiKey, secretKey);
			
			JSONObject paymentLinkRequest=new JSONObject();
			
			paymentLinkRequest.put("amount",order.getTotalDiscountedPrice()*100);
			paymentLinkRequest.put("currency","INR");
			
			JSONObject customer=new JSONObject();
			customer.put("name",order.getUser().getFirstName());
			customer.put("email",order.getUser().getEmail());
			paymentLinkRequest.put("customer",customer);
			
			JSONObject notify=new JSONObject();
			notify.put("sms",true);
			notify.put("email",true);
			paymentLinkRequest.put("notify",notify);
			
			paymentLinkRequest.put("callback_url","https://ecommerce-frontend-seven-smoky.vercel.app/payment/"+order.getId());
			paymentLinkRequest.put("callback_method","get");

			System.out.println("Request => " + paymentLinkRequest.toString());
			System.out.println("KEY LENGTH = " + apiKey.length());
            System.out.println("SECRET LENGTH = " + secretKey.length());
            System.out.println("KEY = [" + apiKey + "]");
            System.out.println("SECRET = [" + secretKey + "]");
			PaymentLink payment=razorpay.paymentLink.create(paymentLinkRequest);
			String paymentLinkId=payment.get("id");
			String paymentLinkUrl=payment.get("short_url");
			PaymentLinkResponse response=new PaymentLinkResponse();
			response.setPayment_Link_Id(paymentLinkId);
			response.setPayment_Link_Url(paymentLinkUrl);
			System.out.println("Responsse is :: "+response);
			return new ResponseEntity<PaymentLinkResponse>(response,HttpStatus.CREATED);
		}catch(Exception e) {
			 e.printStackTrace();
             System.out.println("REAL ERROR => " + e.getMessage());
			throw new RazorpayException("Error creating payment link", e);
		}
		
	}
	
	public ResponseEntity<ApiResponse> redirect(@RequestParam(name="payment_id") String paymentId,@RequestParam
			(name="order_id")Long orderId) throws OrderException, RazorpayException{
		Order order=orderService.findOrderById(orderId);
		RazorpayClient razorpay=new RazorpayClient(apiKey, secretKey);
		try {
			Payment payment=razorpay.payments.fetch(paymentId);
			if(payment.get("status").equals("captured")) {
			order.getPaymentDetails().setPaymentId(paymentId);
			order.getPaymentDetails().setPaymentStatus("COMPLETED");
			order.setOrderStatus("PLACED");
			}
			orderRepo.save(order);
			ApiResponse response=new ApiResponse("your order got palaced",true);
			return new ResponseEntity<ApiResponse>(response,HttpStatus.ACCEPTED);
		}catch(Exception e) {
			throw new RazorpayException(e.getMessage());
		}
	}
	
	@GetMapping("/payments/confirm")
	public ResponseEntity<ApiResponse> confirmPayment(
	        @RequestParam("razorpay_payment_id") String paymentId,
	        @RequestParam("order_id") Long orderId
	) throws OrderException, RazorpayException {

	    Order order = orderService.findOrderById(orderId);
	    RazorpayClient razorpay = new RazorpayClient(apiKey, secretKey);

	    try {
	        Payment payment = razorpay.payments.fetch(paymentId);

	        if (payment.get("status").equals("captured")) {

	            order.getPaymentDetails().setPaymentId(paymentId);
	            order.getPaymentDetails().setPaymentStatus("COMPLETED");

	            order.setOrderStatus("PLACED");

	            orderRepo.save(order);
	        }

	        return new ResponseEntity<>(
	                new ApiResponse("Payment Confirmed", true),
	                HttpStatus.OK
	        );

	    } catch (Exception e) {
	        throw new RazorpayException(e.getMessage());
	    }
	}

}
