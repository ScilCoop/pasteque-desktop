<?php
class Pagination {

public $nbtotal; 
public $name; 
public $nbmaxparpage;
public $nbdepages; 
public $minid; 
public $pageencours; 
public function __construct( $nbtotal, $nbmaxparpage = 10, $name = 'page') {
global $app;
$request = $app['request'];
$this->nbtotal = (int) $nbtotal;
$this->nbmaxparpage = (int) $nbmaxparpage;
$this->nbdepages = ceil($this->nbtotal / $this->nbmaxparpage);
$this->name = $name;
$num_page=intval($request->get($this->name));
$this->pageencours = ( (int) $num_page > 1 ) ? (int) $num_page : 1;
$this->minid = ( $this->pageencours - 1 ) * $this->nbmaxparpage;



}


}
?> 