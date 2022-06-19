/*
 Navicat Premium Data Transfer

 Source Server         : james
 Source Server Type    : MySQL
 Source Server Version : 50738
 Source Host           : localhost:3306
 Source Schema         : james

 Target Server Type    : MySQL
 Target Server Version : 50738
 File Encoding         : 65001

 Date: 19/06/2022 21:11:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for invoice
-- ----------------------------
DROP TABLE IF EXISTS `invoice`;
CREATE TABLE `invoice`  (
  `id` bigint(20) NOT NULL,
  `product_buyer_id` bigint(20) NULL DEFAULT NULL,
  `recipient` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of invoice
-- ----------------------------
INSERT INTO `invoice` VALUES (1, 1, 'kobe');

SET FOREIGN_KEY_CHECKS = 1;
