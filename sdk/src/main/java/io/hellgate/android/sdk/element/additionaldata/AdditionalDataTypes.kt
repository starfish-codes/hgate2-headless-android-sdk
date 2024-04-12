package io.hellgate.android.sdk.element.additionaldata

enum class AdditionalDataTypes(internal val label: String) {
    CARDHOLDER_NAME("Cardholder Name"),
    EMAIL("E-Mail"),
    BILLING_ADDRESS_LINE_1("Billing Address Line 1"),
    BILLING_ADDRESS_LINE_2("Billing Address Line 2"),
    BILLING_ADDRESS_LINE_3("Billing Address Line 3"),
    BILLING_ADDRESS_POSTAL_CODE("Postal Code"),
    BILLING_ADDRESS_CITY("City"),
    BILLING_ADDRESS_COUNTRY("Country"),
    ;

    fun getLabel(): String = label
}
