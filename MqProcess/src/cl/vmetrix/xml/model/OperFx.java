//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.08.06 at 11:41:09 AM PYT 
//


package cl.vmetrix.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="campos">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="origen" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfParMonedas" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfTipoProducto" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfCanalTrx" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfMedioTrx" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfUsuario" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfNumeroOpFindur" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="opfCodSucOrigen" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="opfRutCliente" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfSecuenciaCliente" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="opfTipoCliente" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfNombreCliente" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfMonedaCompra" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfMonedaVenta" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfMontoCompra" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="opfMontoVenta" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="opfEquivMontoCompraCLP" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="opfEquivMontoVentaCLP" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="opfTCCierreUSD" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="opfParCierreUSDME" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="opfCostoFondoUSD" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="opfParCostoFondoUSDME" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="opfPrecioCliente" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="opfPrecioCosto" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="opfFPagoCompra" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfFPagoVenta" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfValutaCompra" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfValutaVenta" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfIndTipoOp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfNumPasaporte" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfPaisPago" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="opfIndicadorFX" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="origen">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="fecha" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="hora" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="sistema" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "campos",
    "origen"
})
@XmlRootElement(name = "OperFx")
public class OperFx {

    @XmlElement(required = true)
    protected OperFx.Campos campos;
    @XmlElement(required = true)
    protected OperFx.Origen origen;

    /**
     * Gets the value of the campos property.
     * 
     * @return
     *     possible object is
     *     {@link OperFx.Campos }
     *     
     */
    public OperFx.Campos getCampos() {
        return campos;
    }

    /**
     * Sets the value of the campos property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperFx.Campos }
     *     
     */
    public void setCampos(OperFx.Campos value) {
        this.campos = value;
    }

    /**
     * Gets the value of the origen property.
     * 
     * @return
     *     possible object is
     *     {@link OperFx.Origen }
     *     
     */
    public OperFx.Origen getOrigen() {
        return origen;
    }

    /**
     * Sets the value of the origen property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperFx.Origen }
     *     
     */
    public void setOrigen(OperFx.Origen value) {
        this.origen = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="origen" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfParMonedas" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfTipoProducto" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfCanalTrx" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfMedioTrx" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfUsuario" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfNumeroOpFindur" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="opfCodSucOrigen" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="opfRutCliente" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfSecuenciaCliente" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="opfTipoCliente" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfNombreCliente" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfMonedaCompra" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfMonedaVenta" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfMontoCompra" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="opfMontoVenta" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="opfEquivMontoCompraCLP" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="opfEquivMontoVentaCLP" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="opfTCCierreUSD" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="opfParCierreUSDME" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="opfCostoFondoUSD" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="opfParCostoFondoUSDME" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="opfPrecioCliente" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="opfPrecioCosto" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="opfFPagoCompra" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfFPagoVenta" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfValutaCompra" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfValutaVenta" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfIndTipoOp" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfNumPasaporte" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfPaisPago" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="opfIndicadorFX" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "origen",
        "opfParMonedas",
        "opfTipoProducto",
        "opfCanalTrx",
        "opfMedioTrx",
        "opfUsuario",
        "opfNumeroOpFindur",
        "opfCodSucOrigen",
        "opfRutCliente",
        "opfSecuenciaCliente",
        "opfTipoCliente",
        "opfNombreCliente",
        "opfMonedaCompra",
        "opfMonedaVenta",
        "opfMontoCompra",
        "opfMontoVenta",
        "opfEquivMontoCompraCLP",
        "opfEquivMontoVentaCLP",
        "opfTCCierreUSD",
        "opfParCierreUSDME",
        "opfCostoFondoUSD",
        "opfParCostoFondoUSDME",
        "opfPrecioCliente",
        "opfPrecioCosto",
        "opfFPagoCompra",
        "opfFPagoVenta",
        "opfValutaCompra",
        "opfValutaVenta",
        "opfIndTipoOp",
        "opfNumPasaporte",
        "opfPaisPago",
        "opfIndicadorFX"
    })
    public static class Campos {

        @XmlElement(required = true)
        protected String origen;
        @XmlElement(required = true)
        protected String opfParMonedas;
        @XmlElement(required = true)
        protected String opfTipoProducto;
        @XmlElement(required = true)
        protected String opfCanalTrx;
        @XmlElement(required = true)
        protected String opfMedioTrx;
        @XmlElement(required = true)
        protected String opfUsuario;
        protected int opfNumeroOpFindur;
        protected int opfCodSucOrigen;
        @XmlElement(required = true)
        protected String opfRutCliente;
        protected int opfSecuenciaCliente;
        @XmlElement(required = true)
        protected String opfTipoCliente;
        @XmlElement(required = true)
        protected String opfNombreCliente;
        @XmlElement(required = true)
        protected String opfMonedaCompra;
        @XmlElement(required = true)
        protected String opfMonedaVenta;
        protected double opfMontoCompra;
        protected double opfMontoVenta;
        protected int opfEquivMontoCompraCLP;
        protected int opfEquivMontoVentaCLP;
        protected double opfTCCierreUSD;
        protected double opfParCierreUSDME;
        protected double opfCostoFondoUSD;
        protected double opfParCostoFondoUSDME;
        protected double opfPrecioCliente;
        protected double opfPrecioCosto;
        @XmlElement(required = true)
        protected String opfFPagoCompra;
        @XmlElement(required = true)
        protected String opfFPagoVenta;
        @XmlElement(required = true)
        protected String opfValutaCompra;
        @XmlElement(required = true)
        protected String opfValutaVenta;
        @XmlElement(required = true)
        protected String opfIndTipoOp;
        @XmlElement(required = true)
        protected String opfNumPasaporte;
        @XmlElement(required = true)
        protected String opfPaisPago;
        @XmlElement(required = true)
        protected String opfIndicadorFX;

        /**
         * Gets the value of the origen property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOrigen() {
            return origen;
        }

        /**
         * Sets the value of the origen property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOrigen(String value) {
            this.origen = value;
        }

        /**
         * Gets the value of the opfParMonedas property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfParMonedas() {
            return opfParMonedas;
        }

        /**
         * Sets the value of the opfParMonedas property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfParMonedas(String value) {
            this.opfParMonedas = value;
        }

        /**
         * Gets the value of the opfTipoProducto property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfTipoProducto() {
            return opfTipoProducto;
        }

        /**
         * Sets the value of the opfTipoProducto property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfTipoProducto(String value) {
            this.opfTipoProducto = value;
        }

        /**
         * Gets the value of the opfCanalTrx property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfCanalTrx() {
            return opfCanalTrx;
        }

        /**
         * Sets the value of the opfCanalTrx property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfCanalTrx(String value) {
            this.opfCanalTrx = value;
        }

        /**
         * Gets the value of the opfMedioTrx property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfMedioTrx() {
            return opfMedioTrx;
        }

        /**
         * Sets the value of the opfMedioTrx property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfMedioTrx(String value) {
            this.opfMedioTrx = value;
        }

        /**
         * Gets the value of the opfUsuario property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfUsuario() {
            return opfUsuario;
        }

        /**
         * Sets the value of the opfUsuario property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfUsuario(String value) {
            this.opfUsuario = value;
        }

        /**
         * Gets the value of the opfNumeroOpFindur property.
         * 
         */
        public int getOpfNumeroOpFindur() {
            return opfNumeroOpFindur;
        }

        /**
         * Sets the value of the opfNumeroOpFindur property.
         * 
         */
        public void setOpfNumeroOpFindur(int value) {
            this.opfNumeroOpFindur = value;
        }

        /**
         * Gets the value of the opfCodSucOrigen property.
         * 
         */
        public int getOpfCodSucOrigen() {
            return opfCodSucOrigen;
        }

        /**
         * Sets the value of the opfCodSucOrigen property.
         * 
         */
        public void setOpfCodSucOrigen(int value) {
            this.opfCodSucOrigen = value;
        }

        /**
         * Gets the value of the opfRutCliente property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfRutCliente() {
            return opfRutCliente;
        }

        /**
         * Sets the value of the opfRutCliente property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfRutCliente(String value) {
            this.opfRutCliente = value;
        }

        /**
         * Gets the value of the opfSecuenciaCliente property.
         * 
         */
        public int getOpfSecuenciaCliente() {
            return opfSecuenciaCliente;
        }

        /**
         * Sets the value of the opfSecuenciaCliente property.
         * 
         */
        public void setOpfSecuenciaCliente(int value) {
            this.opfSecuenciaCliente = value;
        }

        /**
         * Gets the value of the opfTipoCliente property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfTipoCliente() {
            return opfTipoCliente;
        }

        /**
         * Sets the value of the opfTipoCliente property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfTipoCliente(String value) {
            this.opfTipoCliente = value;
        }

        /**
         * Gets the value of the opfNombreCliente property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfNombreCliente() {
            return opfNombreCliente;
        }

        /**
         * Sets the value of the opfNombreCliente property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfNombreCliente(String value) {
            this.opfNombreCliente = value;
        }

        /**
         * Gets the value of the opfMonedaCompra property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfMonedaCompra() {
            return opfMonedaCompra;
        }

        /**
         * Sets the value of the opfMonedaCompra property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfMonedaCompra(String value) {
            this.opfMonedaCompra = value;
        }

        /**
         * Gets the value of the opfMonedaVenta property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfMonedaVenta() {
            return opfMonedaVenta;
        }

        /**
         * Sets the value of the opfMonedaVenta property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfMonedaVenta(String value) {
            this.opfMonedaVenta = value;
        }

        /**
         * Gets the value of the opfMontoCompra property.
         * 
         */
        public double getOpfMontoCompra() {
            return opfMontoCompra;
        }

        /**
         * Sets the value of the opfMontoCompra property.
         * 
         */
        public void setOpfMontoCompra(double value) {
            this.opfMontoCompra = value;
        }

        /**
         * Gets the value of the opfMontoVenta property.
         * 
         */
        public double getOpfMontoVenta() {
            return opfMontoVenta;
        }

        /**
         * Sets the value of the opfMontoVenta property.
         * 
         */
        public void setOpfMontoVenta(double value) {
            this.opfMontoVenta = value;
        }

        /**
         * Gets the value of the opfEquivMontoCompraCLP property.
         * 
         */
        public int getOpfEquivMontoCompraCLP() {
            return opfEquivMontoCompraCLP;
        }

        /**
         * Sets the value of the opfEquivMontoCompraCLP property.
         * 
         */
        public void setOpfEquivMontoCompraCLP(int value) {
            this.opfEquivMontoCompraCLP = value;
        }

        /**
         * Gets the value of the opfEquivMontoVentaCLP property.
         * 
         */
        public int getOpfEquivMontoVentaCLP() {
            return opfEquivMontoVentaCLP;
        }

        /**
         * Sets the value of the opfEquivMontoVentaCLP property.
         * 
         */
        public void setOpfEquivMontoVentaCLP(int value) {
            this.opfEquivMontoVentaCLP = value;
        }

        /**
         * Gets the value of the opfTCCierreUSD property.
         * 
         */
        public double getOpfTCCierreUSD() {
            return opfTCCierreUSD;
        }

        /**
         * Sets the value of the opfTCCierreUSD property.
         * 
         */
        public void setOpfTCCierreUSD(double value) {
            this.opfTCCierreUSD = value;
        }

        /**
         * Gets the value of the opfParCierreUSDME property.
         * 
         */
        public double getOpfParCierreUSDME() {
            return opfParCierreUSDME;
        }

        /**
         * Sets the value of the opfParCierreUSDME property.
         * 
         */
        public void setOpfParCierreUSDME(double value) {
            this.opfParCierreUSDME = value;
        }

        /**
         * Gets the value of the opfCostoFondoUSD property.
         * 
         */
        public double getOpfCostoFondoUSD() {
            return opfCostoFondoUSD;
        }

        /**
         * Sets the value of the opfCostoFondoUSD property.
         * 
         */
        public void setOpfCostoFondoUSD(double value) {
            this.opfCostoFondoUSD = value;
        }

        /**
         * Gets the value of the opfParCostoFondoUSDME property.
         * 
         */
        public double getOpfParCostoFondoUSDME() {
            return opfParCostoFondoUSDME;
        }

        /**
         * Sets the value of the opfParCostoFondoUSDME property.
         * 
         */
        public void setOpfParCostoFondoUSDME(double value) {
            this.opfParCostoFondoUSDME = value;
        }

        /**
         * Gets the value of the opfPrecioCliente property.
         * 
         */
        public double getOpfPrecioCliente() {
            return opfPrecioCliente;
        }

        /**
         * Sets the value of the opfPrecioCliente property.
         * 
         */
        public void setOpfPrecioCliente(double value) {
            this.opfPrecioCliente = value;
        }

        /**
         * Gets the value of the opfPrecioCosto property.
         * 
         */
        public double getOpfPrecioCosto() {
            return opfPrecioCosto;
        }

        /**
         * Sets the value of the opfPrecioCosto property.
         * 
         */
        public void setOpfPrecioCosto(double value) {
            this.opfPrecioCosto = value;
        }

        /**
         * Gets the value of the opfFPagoCompra property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfFPagoCompra() {
            return opfFPagoCompra;
        }

        /**
         * Sets the value of the opfFPagoCompra property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfFPagoCompra(String value) {
            this.opfFPagoCompra = value;
        }

        /**
         * Gets the value of the opfFPagoVenta property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfFPagoVenta() {
            return opfFPagoVenta;
        }

        /**
         * Sets the value of the opfFPagoVenta property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfFPagoVenta(String value) {
            this.opfFPagoVenta = value;
        }

        /**
         * Gets the value of the opfValutaCompra property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfValutaCompra() {
            return opfValutaCompra;
        }

        /**
         * Sets the value of the opfValutaCompra property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfValutaCompra(String value) {
            this.opfValutaCompra = value;
        }

        /**
         * Gets the value of the opfValutaVenta property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfValutaVenta() {
            return opfValutaVenta;
        }

        /**
         * Sets the value of the opfValutaVenta property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfValutaVenta(String value) {
            this.opfValutaVenta = value;
        }

        /**
         * Gets the value of the opfIndTipoOp property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfIndTipoOp() {
            return opfIndTipoOp;
        }

        /**
         * Sets the value of the opfIndTipoOp property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfIndTipoOp(String value) {
            this.opfIndTipoOp = value;
        }

        /**
         * Gets the value of the opfNumPasaporte property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfNumPasaporte() {
            return opfNumPasaporte;
        }

        /**
         * Sets the value of the opfNumPasaporte property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfNumPasaporte(String value) {
            this.opfNumPasaporte = value;
        }

        /**
         * Gets the value of the opfPaisPago property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfPaisPago() {
            return opfPaisPago;
        }

        /**
         * Sets the value of the opfPaisPago property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfPaisPago(String value) {
            this.opfPaisPago = value;
        }

        /**
         * Gets the value of the opfIndicadorFX property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOpfIndicadorFX() {
            return opfIndicadorFX;
        }

        /**
         * Sets the value of the opfIndicadorFX property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOpfIndicadorFX(String value) {
            this.opfIndicadorFX = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *       &lt;attribute name="fecha" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="hora" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="sistema" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Origen {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "fecha")
        protected String fecha;
        @XmlAttribute(name = "hora")
        protected String hora;
        @XmlAttribute(name = "sistema")
        protected String sistema;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the fecha property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFecha() {
            return fecha;
        }

        /**
         * Sets the value of the fecha property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFecha(String value) {
            this.fecha = value;
        }

        /**
         * Gets the value of the hora property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getHora() {
            return hora;
        }

        /**
         * Sets the value of the hora property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHora(String value) {
            this.hora = value;
        }

        /**
         * Gets the value of the sistema property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSistema() {
            return sistema;
        }

        /**
         * Sets the value of the sistema property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSistema(String value) {
            this.sistema = value;
        }

    }

}
