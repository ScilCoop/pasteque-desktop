<?php
//    POS-Tech API
//
//    Copyright (C) 2012 Scil (http://scil.coop)
//
//    This file is part of POS-Tech.
//
//    POS-Tech is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    POS-Tech is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with POS-Tech.  If not, see <http://www.gnu.org/licenses/>.

class TicketLineLight {

    public $line;
    public $productId;
    public $quantity;
    public $price;
    public $taxId;

    function __construct($line, $productId, $quantity, $price, $taxId) {
        $this->line = $line;
        $this->productId = $productId;
        $this->quantity = $quantity;
        $this->price = $price;
        $this->taxId = $taxId;
    }

    function getSubtotal() {
        return $this->price * $this->quantity;
    }
}

class TicketLine {

    public $line;
    public $product;
    public $quantity;
    public $price;
    public $tax;
    public $attributes;

    function __construct($line, $product, $quantity, $price, $tax) {
        $this->line = $line;
        $this->product = $product;
        $this->quantity = $quantity;
        $this->price = $price;
        $this->tax = $tax;
        $this->createAttributes();
    }

    function getSubtotal() {
        return $this->price * $this->quantity;
    }

    /** Build xml attributes from line data. See TicketLineInfo constructors. */
    private function createAttributes() {
        // Set xml
        $domimpl = new DOMImplementation();
        $doctype = $domimpl->createDocumentType('properties', null,
                                                "http://java.sun.com/dtd/properties.dtd");
        $attrs = $domimpl->createDocument(null, null, $doctype);
        $attrs->encoding = "UTF-8";
        $attrs->version = "1.0";
        $attrs->standalone = false;
        // Add root properties element
        $properties = $attrs->createElement("properties");
        $attrs->appendChild($properties);
        // Add comment element
        $comment = $attrs->createElement("comment");
        $comment->appendChild($attrs->createTextNode("POS-Tech")); // This is actually the application name
        $properties->appendChild($comment);
        // Add some product keys
        $entry = $attrs->createElement("entry");
        $key = $attrs->createAttribute("key");
        $key->appendChild($attrs->createTextNode("product.taxcategoryid"));
        $entry->appendChild($key);
        $entry->appendChild($attrs->createTextNode($this->tax->tax_cat_id));
        $properties->appendChild($entry);
        $entry = $attrs->createElement("entry");
        $key = $attrs->createAttribute("key");
        $key->appendChild($attrs->createTextNode("product.com"));
        $entry->appendChild($key);
        $entry->appendChild($attrs->createTextNode("false")); // TODO add iscom field
        $properties->appendChild($entry);
        $entry = $attrs->createElement("entry");
        $key = $attrs->createAttribute("key");
        $key->appendChild($attrs->createTextNode("product.categoryid"));
        $entry->appendChild($key);
        $entry->appendChild($attrs->createTextNode($this->product->category->id));
        $properties->appendChild($entry);
        $entry = $attrs->createElement("entry");
        $key = $attrs->createAttribute("key");
        $key->appendChild($attrs->createTextNode("product.scale"));
        $entry->appendChild($key);
        $entry->appendChild($attrs->createTextNode(strval($this->product->scaled)?"true":"false"));
        $properties->appendChild($entry);
        $entry = $attrs->createElement("entry");
        $key = $attrs->createAttribute("key");
        $key->appendChild($attrs->createTextNode("product.name"));
        $entry->appendChild($key);
        $entry->appendChild($attrs->createTextNode($this->product->label));
        $properties->appendChild($entry);
        // Save all this stuff
        $this->attributes = $attrs->saveXML();
    }

}

?>
