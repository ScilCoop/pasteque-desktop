<?php
class searchbar {

public $name; 
public $where;
public $search;


private function getSearchColumns($object='product')
{
$tab_cols=array();
switch($object)
{
	case 'product':
		$tab_cols=array('name','code','reference');
	break;

}
return $tab_cols;
}



public function __construct( $object='product',$name = 'search') {
global $app;
$request = $app['request'];
$this->name = $name;
$form = $app['form.factory']
->createbuilder('form')
->add($this->name, 'text', array('label' => 'Rechercher'))
->getForm();
if ('GET' == $app['request']->getMethod()) {
        $form->bindRequest($request);
        if ($form->isValid()) {
            $data = $form->getData();
            $this->search=$data['search'];
        }
    }
$tab_cols=$this->getSearchColumns();


if(!empty($this->search) && !empty($tab_cols))
	$this->where='('.implode(' like \'%'.$this->search.'%\' OR ',$tab_cols).' like \'%'.$this->search.'%\')';
}

public function render()
{
global $app;
$data=array($this->name => $this->search);
$form = $app['form.factory']
->createbuilder('form',$data)
->add($this->name, 'text', array('label' => 'Rechercher'))
->getForm();

return $app['twig']->render('searchbar.twig', array('form'=>$form->createView(),'name'=>$this->name ));
}

public function constructForm()
{
global $app;
$data=array($this->name => $this->search);
$form = $app['form.factory']
->createbuilder('form',$data)
->add($this->name, 'text', array('label' => 'Rechercher'))
->getForm();

return $app['twig']->render('searchbar.twig', array('form'=>$form->createView(),'name'=>$this->name ));
}



}
?> 