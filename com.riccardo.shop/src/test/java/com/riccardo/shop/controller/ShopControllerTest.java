package com.riccardo.shop.controller;

import static org.junit.Assert.*;

import org.junit.Test;

public class ShopControllerTest {

	@Test
	public void shouldReturnOkStatus() {
		ShopController controller = new ShopController();

		assertEquals("ok", controller.getStatus());
	}

}
