<?php

ini_set('display_errors', 1);
error_reporting(-1);
$debug=true;

require_once __DIR__.'/vendor/autoload.php';
$app = new Silex\Application();

// Register extensions
$app->register(new Silex\Provider\TwigServiceProvider(), array(
    'twig.path' => __DIR__.'/views'));

$app->register(new Silex\Provider\UrlGeneratorServiceProvider());

$app->register(new Silex\Provider\FormServiceProvider());

$app->register(new Silex\Provider\TranslationServiceProvider(), array(
  'locale_fallback' => 'fr',
  'translator.messages' => array()
));





// Add layout
$app->before(function () use ($app) {
    $app['twig']->addGlobal('layout', $app['twig']->loadTemplate('layout.twig'));
});




$app['debug'] = $debug;


$app->get('/hello', function() {
    return 'Hello!';
});



// Add pages
$pages = array(
    '/' => 'home',
    '/catalog' => 'catalog',
    '/contact' => 'contact',
    '/suppliers' => 'suppliers',
    '/categories' => 'categories',
    '/about' => 'about'
);

foreach ($pages as $route => $view) {
    $app->get($route, function () use ($app, $view) {
        return $app['twig']->render($view . '.twig');
    })->bind($view);
}



$app_pages=array('taxes','taxes_edit','taxes_delete','products_list',
				'product_edit','product','product_delete');

foreach ($app_pages as $view) {

	$app->get('/'.$view, function () use ($app, $view) {
		return require_once __DIR__ . '/'.$view.'.php';
	})->bind($view);

}






$app->match('/contact/{sent}', function ($sent) use ($app) {
	return require_once __DIR__ . '/contact.php';
})
    ->convert('sent', function ($sent) { return (bool) $sent; })
    ->value('sent', false)
    ->bind('contact');




$app->run();
