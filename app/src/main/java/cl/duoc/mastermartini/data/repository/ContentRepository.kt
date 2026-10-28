package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.model.Difficulty
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.data.model.LearnKind
import cl.duoc.mastermartini.data.model.Order
import cl.duoc.mastermartini.data.model.OrderStep
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.ServiceItem
import cl.duoc.mastermartini.data.model.ServiceType

/**
 * Fuente de contenido de la app. Hoy entrega datos ficticios en memoria;
 * en la EFT se puede reemplazar por una API REST sin tocar las pantallas,
 * porque los ViewModels solo conocen esta interfaz.
 */
interface ContentRepository {
    fun learnItems(): List<LearnItem>
    fun learnItem(id: String): LearnItem?
    fun products(): List<Product>
    fun product(id: String): Product?
    fun productCategories(): List<String>
    fun services(): List<ServiceItem>
    fun service(id: String): ServiceItem?
    fun sampleOrders(): List<Order>
    fun salesPhone(): String
    fun learnCategoriesFor(type: BusinessType): List<String>
}

class FakeContentRepository : ContentRepository {

    private val learn = listOf(
        LearnItem(
            id = "v-templado", kind = LearnKind.VIDEO, title = "Templado de chocolate paso a paso",
            category = "Chocolatería", duration = "12 min", difficulty = Difficulty.EASY, isNew = true,
            description = "Aprende la curva de temperatura correcta para lograr brillo y quiebre en tus bombones.",
            steps = listOf(
                "Funde la cobertura a 45–50 °C.",
                "Baja la temperatura a 27–28 °C moviendo sobre mármol.",
                "Sube a 31–32 °C y comprueba con una espátula.",
            ),
            productIds = listOf("p-cobertura", "p-manteca-cacao"), thumbColor = 0xFF3B2A22,
        ),
        LearnItem(
            id = "v-laminado", kind = LearnKind.VIDEO, title = "Laminado de masa de hoja",
            category = "Panadería", duration = "18 min", difficulty = Difficulty.MEDIUM,
            description = "Pliegues simples y dobles para una masa de hoja pareja y crujiente.",
            steps = listOf("Prepara el empaste.", "Envuelve la margarina.", "Realiza 3 vueltas dobles con reposo en frío."),
            productIds = listOf("p-margarina"), thumbColor = 0xFF8A5A3C,
        ),
        LearnItem(
            id = "v-helado", kind = LearnKind.VIDEO, title = "Base de helado italiano",
            category = "Heladería", duration = "9 min", difficulty = Difficulty.EASY,
            description = "Balanceo de sólidos y azúcares para una base cremosa.",
            steps = listOf("Mezcla secos.", "Pasteuriza a 85 °C.", "Madura 4 horas en frío y mantecar."),
            productIds = listOf("p-base-helado", "p-crema"), thumbColor = 0xFFC79A6B,
        ),
        LearnItem(
            id = "r-kuchen", kind = LearnKind.RECIPE, title = "Kuchen de frambuesa",
            category = "Pastelería", duration = "45 min", difficulty = Difficulty.EASY,
            description = "Clásico del sur de Chile con masa quebrada y crema pastelera.",
            steps = listOf("Hornea la masa quebrada en blanco.", "Rellena con crema pastelera.", "Cubre con frambuesas y brillo neutro."),
            productIds = listOf("p-crema", "p-brillo"), thumbColor = 0xFFE8C9A8,
        ),
        LearnItem(
            id = "r-croissant", kind = LearnKind.RECIPE, title = "Croissant de mantequilla",
            category = "Panadería", duration = "3 h", difficulty = Difficulty.MEDIUM,
            description = "Croissant hojaldrado con tres vueltas simples.",
            steps = listOf("Prepara la masa y refrigera.", "Lamina con la materia grasa.", "Forma, fermenta y hornea a 190 °C."),
            productIds = listOf("p-margarina"), thumbColor = 0xFFD9B27C,
        ),
        LearnItem(
            id = "r-bombones", kind = LearnKind.RECIPE, title = "Bombones rellenos de maracuyá",
            category = "Chocolatería", duration = "1 h 30 min", difficulty = Difficulty.HARD,
            description = "Bombones moldeados con ganache de fruta.",
            steps = listOf("Templa y encamisa los moldes.", "Rellena con ganache.", "Sella y desmolda."),
            productIds = listOf("p-cobertura"), thumbColor = 0xFF5A3A2A,
        ),
        LearnItem(
            id = "r-galletas", kind = LearnKind.RECIPE, title = "Galletas de mantequilla para vitrina",
            category = "Galletas", duration = "40 min", difficulty = Difficulty.EASY,
            description = "Masa estable que mantiene la forma al hornear.",
            steps = listOf("Cremar materia grasa y azúcar.", "Incorporar harina.", "Cortar y hornear a 170 °C."),
            productIds = listOf("p-margarina"), thumbColor = 0xFFE0BE8E,
        ),
        LearnItem(
            id = "c-hoja", kind = LearnKind.COURSE, title = "Curso: masas de hoja",
            category = "Panadería", duration = "6 módulos", difficulty = Difficulty.MEDIUM,
            description = "Curso online gratuito con certificado.",
            steps = listOf("Módulo 1: materias grasas", "Módulo 2: empaste", "Módulo 3: vueltas"),
            productIds = listOf("p-margarina"), thumbColor = 0xFFD40000,
        ),
        LearnItem(
            id = "c-vitrina", kind = LearnKind.COURSE, title = "Curso: pastelería de vitrina",
            category = "Pastelería", duration = "8 módulos", difficulty = Difficulty.MEDIUM,
            description = "Montaje, glaseados y conservación de tortas para vitrina.",
            steps = listOf("Módulo 1: bizcochos", "Módulo 2: rellenos", "Módulo 3: decoración"),
            productIds = listOf("p-crema", "p-brillo"), thumbColor = 0xFFB8574A,
        ),
    )

    private val products = listOf(
        Product("p-crema", "Crema vegetal para batir", "Pastelería", listOf("1 L", "10 L"), "Alto rendimiento y estabilidad en vitrina."),
        Product("p-cobertura", "Cobertura semiamarga", "Pastelería", listOf("1 kg", "5 kg"), "Cobertura 54 % cacao para templar."),
        Product("p-margarina", "Margarina para hojaldre", "Panadería", listOf("10 kg"), "Plasticidad ideal para laminados."),
        Product("p-brillo", "Brillo neutro", "Pastelería", listOf("1 kg", "6 kg"), "Gel de terminación en frío."),
        Product("p-manteca-cacao", "Manteca de cacao", "Pastelería", listOf("500 g", "3 kg"), "Para templado y pistolado."),
        Product("p-mejorador", "Mejorador de masas", "Panadería", listOf("1 kg", "10 kg"), "Mayor volumen y miga suave."),
        Product("p-base-helado", "Base para helado italiano", "Gelatería", listOf("2 kg"), "Base neutra en polvo."),
        Product("p-pasta-pistacho", "Pasta de pistacho", "Gelatería", listOf("1 kg"), "Saborizante concentrado."),
        Product("p-salsa-bechamel", "Salsa bechamel lista", "Food service", listOf("1 kg", "5 kg"), "Lista para usar en casino."),
    )

    private val services = listOf(
        ServiceItem(
            "s-masterclass", ServiceType.EVENT, "Masterclass de heladería", "PRESENCIAL",
            "Santiago · Centro de innovación", dateLabel = "20 de noviembre · 10:00 h", takenSeats = 12, totalSeats = 20,
        ),
        ServiceItem(
            "s-asesoria", ServiceType.ADVISORY, "Asesoría técnica con un chef", "ASESORÍA",
            "30 min por videollamada o en tu local",
        ),
        ServiceItem(
            "s-curso-hoja", ServiceType.COURSE, "Curso: masas de hoja", "ONLINE · GRATIS",
            "6 módulos a tu ritmo", progress = 0.4f, hasCertificate = true, learnId = "c-hoja",
        ),
        ServiceItem(
            "s-feria", ServiceType.EVENT, "Feria de innovación en panadería", "PRESENCIAL",
            "Concepción · Casa matriz regional", dateLabel = "5 de diciembre · 09:30 h", takenSeats = 37, totalSeats = 60,
        ),
        ServiceItem(
            "s-curso-vitrina", ServiceType.COURSE, "Curso: pastelería de vitrina", "ONLINE · GRATIS",
            "8 módulos a tu ritmo", progress = 0f, hasCertificate = true, learnId = "c-vitrina",
        ),
    )

    override fun learnItems() = learn
    override fun learnItem(id: String) = learn.firstOrNull { it.id == id }
    override fun products() = products
    override fun product(id: String) = products.firstOrNull { it.id == id }
    override fun productCategories() = products.map { it.category }.distinct()
    override fun services() = services
    override fun service(id: String) = services.firstOrNull { it.id == id }
    override fun salesPhone() = "+56912345678"

    override fun learnCategoriesFor(type: BusinessType): List<String> = when (type) {
        BusinessType.PASTELERIA -> listOf("Pastelería", "Chocolatería", "Galletas")
        BusinessType.PANADERIA -> listOf("Panadería", "Galletas")
        BusinessType.CHOCOLATERIA -> listOf("Chocolatería", "Pastelería")
        BusinessType.HELADERIA -> listOf("Heladería", "Chocolatería")
        BusinessType.RESTAURANTE -> listOf("Pastelería", "Panadería", "Heladería")
    }

    override fun sampleOrders() = listOf(
        Order(
            code = "MM-0142", productCount = 2, delivered = false, deliveredDate = null,
            steps = listOf(
                OrderStep("Recibido", "18 oct", done = true, current = false),
                OrderStep("Confirmado por ventas", "19 oct", done = true, current = false),
                OrderStep("En despacho · llega mañana", null, done = false, current = true),
                OrderStep("Entregado", null, done = false, current = false),
            ),
        ),
        Order(code = "MM-0128", productCount = 3, delivered = true, deliveredDate = "2 oct", steps = emptyList()),
    )
}
