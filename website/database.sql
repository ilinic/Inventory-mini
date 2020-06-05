-- phpMyAdmin SQL Dump
-- version 4.7.7
-- https://www.phpmyadmin.net/
--
-- Хост: localhost
-- Время создания: Июн 05 2020 г., 05:42
-- Версия сервера: 5.7.21-20-beget-5.7.21-20-1-log
-- Версия PHP: 5.6.40

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- --------------------------------------------------------

--
-- Структура таблицы `config`
--
-- Создание: Май 24 2020 г., 02:50
-- Последнее обновление: Июн 05 2020 г., 02:17
--

DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `param` varchar(256) NOT NULL,
  `value` varchar(512) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Дамп данных таблицы `config`
--

INSERT INTO `config` (`param`, `value`) VALUES
('user_list_changed', '0'),
('product_list_changed', '0');

-- --------------------------------------------------------

--
-- Структура таблицы `history`
--
-- Создание: Май 24 2020 г., 12:16
-- Последнее обновление: Июн 05 2020 г., 02:16
--

DROP TABLE IF EXISTS `history`;
CREATE TABLE `history` (
  `product` varchar(256) NOT NULL,
  `user` varchar(256) NOT NULL,
  `actiondate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delta` int(11) NOT NULL,
  `count_before` int(11) NOT NULL,
  `count_after` int(11) NOT NULL,
  `prodid` varchar(128) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Структура таблицы `products`
--
-- Создание: Май 24 2020 г., 11:10
-- Последнее обновление: Июн 05 2020 г., 02:16
--

DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` varchar(128) NOT NULL,
  `prodname` varchar(512) NOT NULL,
  `count` int(11) NOT NULL DEFAULT '0',
  `update_helper` tinyint(4) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Структура таблицы `users`
--
-- Создание: Май 24 2020 г., 06:43
-- Последнее обновление: Июн 05 2020 г., 02:17
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` varchar(128) NOT NULL,
  `username` varchar(128) NOT NULL,
  `email` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `trn_date` datetime DEFAULT NULL,
  `update_helper` tinyint(4) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Дамп данных таблицы `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password`, `trn_date`, `update_helper`) VALUES
('134a30', 'John Smith', NULL, NULL, NULL, 1),
('e2', 'Roy White', NULL, NULL, NULL, 1);

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `history`
--
ALTER TABLE `history`
  ADD KEY `timedateidx` (`actiondate`);

--
-- Индексы таблицы `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`);

--
-- Индексы таблицы `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;