package org.loed.framework;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/7 10:02 上午
 */
public class LockTest {
	private static class Class1 {
		private Class2 class2;

		public void test() {
			System.out.println("Class1 Test");
			class2.test();
		}
	}

	private static class Class2 {
		private Class1 cLass1;

		public void test() {
			System.out.println("Class2 Test");
			cLass1.test();
		}
	}

	public static void main(String[] args) {
		Class1 cls1 = new Class1();
		Class2 cls2 = new Class2();
		cls1.class2 = cls2;
		cls2.cLass1 = cls1;
		cls1.test();
	}
}
