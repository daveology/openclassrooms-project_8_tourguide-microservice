package tourGuide.config;

/** Users testing initializer.
 */
public class InternalTestHelper {

	// Set this default up to 100,000 for testing
	private static int internalUserNumber = 0;

	/** Setting the amount of users to test.
	 * @param internalUserNumber Integer containing the amount of users.
	 */
	public static void setInternalUserNumber(int internalUserNumber) {

		InternalTestHelper.internalUserNumber = internalUserNumber;
	}

	/** Getting the amount of users to test.
	 * @return Return the amount of users.
	 */
	public static int getInternalUserNumber() {

		return internalUserNumber;
	}
}
