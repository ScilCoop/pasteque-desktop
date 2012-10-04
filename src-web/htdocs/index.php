<?php

ini_set('display_errors', 1);
error_reporting(-1);
$debug=true;

require_once __DIR__.'/vendor/autoload.php';
$app = new Silex\Application();

// Register extensions

$app['symfony.path'] = __DIR__.'/vendor/symfony/src' ; 

$app->register(new Silex\Provider\UrlGeneratorServiceProvider());
$app->register(new Silex\Provider\FormServiceProvider());
$app->register(new Silex\Provider\ValidatorServiceProvider());
$app->register(new Silex\Provider\TwigServiceProvider(), array(
    'twig.path' => __DIR__.'/views'));


$app->register(new Silex\Provider\TranslationServiceProvider(), array(
	'locale' => 'fr',
	'translator.domains' => array(),
));


//$app['translator']->addResource('xlf', $file, 'fr', 'validators');
$app->before(function () use ($app) {
    $app['translator']->addLoader('xlf', new Symfony\Component\Translation\Loader\XliffFileLoader());
    $app['translator']->addLoader('yaml', new Symfony\Component\Translation\Loader\YamlFileLoader());
    
});

    

    $app['translator']->addResource('yaml', __DIR__.'/../locales/pos_messages.yml', 'en');
    //$app['translator']->addResource('yaml', __DIR__.'/locales/de.yml', 'de');
    $app['translator']->addResource('yaml', __DIR__.'/../locales/pos_messages_fr_FR.yml', 'fr');

//$app['translator']->addResource('ini', __DIR__.'/../locales/pos_messages.yaml', 'en');
//$app['translator']->addResource('ini', __DIR__.'/../locales/pos_messages_fr_FR.yaml', 'fr');






// Add layout
$app->before(function () use ($app) {
    $app['twig']->addGlobal('layout', $app['twig']->loadTemplate('layout.twig'));
});



define('NB_ROW_PRODUCT',50);



$app['debug'] = $debug;


$app->get('/hello', function() {
    return 'Hello!';
});



// Add pages
$pages = array(
    '/' => 'home',
    '/catalog' => 'catalog',
    '/contact' => 'contact',
    '/suppliers_list' => 'suppliers_list',
    '/categories_list' => 'categories_list',
    '/about' => 'about'
);

foreach ($pages as $route => $view) {
    $app->get($route, function () use ($app, $view) {
        return $app['twig']->render($view . '.twig');
    })->bind($view);
}



$app_pages=array('taxes_list','taxe','taxe_edit','taxe_delete','products_list',
				'product_edit','product','product_delete','contact',
				'image');

foreach ($app_pages as $view) {

	$app->match('/'.$view, function () use ($app, $view) {
		return require_once __DIR__ . '/'.$view.'.php';
	})
	->bind($view);

}


$app->run();
