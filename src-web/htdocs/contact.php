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


$request = $app['request'];
$sent=$request->get('send');
 
$form = $app['form.factory']
->createBuilder('form')
->add('name', 'text', array('label' => 'Nom:'))
->add('email', 'email', array('label' => 'Email:'))
->add('message', 'textarea', array('label' => 'Message:'))
->getForm();

    if ('POST' == $app['request']->getMethod()) {
        $form->bindRequest($app['request']);
        if ($form->isValid()) {
            $data = $form->getData();

            require_once __DIR__ . '/../vendor/swiftmailer/lib/swift_required.php';
            \Swift_Mailer::newInstance(\Swift_MailTransport::newInstance())
                ->send(\Swift_Message::newInstance()
                    ->setSubject(sprintf('Contact from %s', $_SERVER['SERVER_NAME']))
                    ->setFrom(array($data['email']))
                    ->setTo(array('umpirsky@gmail.com'))
                    ->setBody($data['message'])
                );

            return $app->redirect($app['url_generator']->generate(
                'contact',
                array('sent' => true)
            ));
        }
    }

    return $app['twig']->render('contact.twig', array(
        'form' => $form->createView(),
        'sent' => $sent
    ));



?>