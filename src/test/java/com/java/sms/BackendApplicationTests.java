//package com.java.sms;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class BackendApplicationTests {

//
//	@Test
//	void contextLoads() {
//	}
//

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

////	@Disabled
////	@Test
////	public void add(){
////		assertEquals(5, 5);
////		assertNotNull(userRepository.findByEmail("example@email.com"));
////	}
////
////	@Disabled
////	@ParameterizedTest
////	@CsvSource({
////			"1,1,2",
////			"2,4,6",
////			"55,10,60",
////			"15,15,30"
////	})
////	public void parameter(int a, int b, int target){
////
////		assertEquals(target, a + b);
////	}
//
//	// Example method
//	int add(int a, int b) {
//		return a + b;
//	}
//
//	//  1. assertEquals / assertNotEquals
//	@Test
//	void testAssertEqualsAndNotEquals() {
//		assertEquals(5, add(2, 3), "Addition should be 5");
//		assertNotEquals(6, add(2, 3), "Addition should not be 6");
//	}
//
//	//  2. assertTrue / assertFalse
//	@Test
//	void testAssertTrueAndFalse() {
//		assertTrue(10 > 5, "10 is greater than 5");
//		assertFalse(5 > 10, "5 is not greater than 10");
//	}
//
//	//  3. assertNull / assertNotNull
//	@Test
//	void testAssertNullAndNotNull() {
//		String name = null;
//		String city = "Delhi";
//
//		assertNull(name, "Name should be null");
//		assertNotNull(city, "City should not be null");
//	}
//
//	//  4. assertSame / assertNotSame
//	@Test
//	void testAssertSameAndNotSame() {
//		String s1 = "Hello";
//		String s2 = s1;
//		String s3 = new String("Hello");
//
//		assertSame(s1, s2, "Both should refer to same object");
//		assertNotSame(s1, s3, "Different object references");
//	}
//
//	// 5. assertArrayEquals
//	@Test
//	void testAssertArrayEquals() {
//		int[] expected = {1, 2, 3};
//		int[] actual = {1, 2, 3};
//
//		assertArrayEquals(expected, actual, "Arrays should be equal");
//	}
//
//	//  6. assertIterableEquals
//	@Test
//	void testAssertIterableEquals() {
//		List<String> expected = List.of("A", "B", "C");
//		List<String> actual = List.of("A", "B", "C");
//
//		assertIterableEquals(expected, actual, "Lists should be equal");
//	}
//
//	//  7. assertThrows (for exception testing)
//	@Test
//	void testAssertThrows() {
//		Exception exception = assertThrows(ArithmeticException.class, () -> {
//			int x = 10 / 0; // throws exception
//		});
//		assertEquals("/ by zero", exception.getMessage());
//	}
//
//	//  8. assertDoesNotThrow
//	@Test
//	void testAssertDoesNotThrow() {
//		assertDoesNotThrow(() -> {
//			int result = add(2, 3);
//			System.out.println(result);
//		}, "Should not throw any exception");
//	}
//
//	//  9. assertAll (group multiple assertions)
//	@Test
//	void testAssertAll() {
//		int a = 10;
//		int b = 5;
//		assertAll("Grouped assertions",
//				() -> assertEquals(15, add(a, b)),
//				() -> assertTrue(a > b),
//				() -> assertNotNull(a)
//		);
//	}
//
//	//  10. fail() (force test to fail manually)
//	@Test
//	void testFailExample() {
//		try {
//			int x = 1 / 0;
//			fail("Should have thrown ArithmeticException");
//		} catch (ArithmeticException e) {
//			// Test passes, expected exception
//		}
//	}
//
//}











//@Mock
//private UserRepository userRepository;  // Mock repository
//
//@InjectMocks
//private UserServiceImpl userService;  // Real service with mocked dependency
//
//@Test
//void testGetUserByEmail() {
//    // Arrange
//    String email = "john@example.com";
//    User mockUser = new User();
//    mockUser.setId(1L);
//    mockUser.setUserName("John Doe");
//    mockUser.setEmail(email);
//
//    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
//
//    // Act
//    User result = userService.findByUserEmail(email);
//
//    // Assert
//    assertNotNull(result);
//    assertEquals("John Doe", result.getUserName());
//    assertEquals(email, result.getEmail());
//    verify(userRepository, times(1)).findByEmail(email);
//}

