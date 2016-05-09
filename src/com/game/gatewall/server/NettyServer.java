package com.game.gatewall.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 基于Netty的Socket服务
 * 
 * @author jamlin
 *
 *         2016年5月9日
 */
public class NettyServer {

	/**
	 * 接收socket连接的线程池,线程数采用默认的Runtime.getRuntime().availableProcessors() * 2
	 */
	private EventLoopGroup recevieGroup = new NioEventLoopGroup();

	/**
	 * 处理IO的线程池,线程数采用默认的Runtime.getRuntime().availableProcessors() * 2
	 */
	private EventLoopGroup wokerGroup = new NioEventLoopGroup();

	/**
	 * @param port
	 */
	public NettyServer(int port) {
		try {
			ServerBootstrap bt = new ServerBootstrap()
					.group(recevieGroup, wokerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline()
									.addLast(new ProtobufVarint32FrameDecoder())
									.addLast(
											new ProtobufVarint32LengthFieldPrepender());
						}

					});
			// 绑定端口，同步等待成功
			ChannelFuture f = bt.bind(port).sync();
			// 等待服务端监听端口关闭
			f.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 退出时释放资源
			recevieGroup.shutdownGracefully();
			wokerGroup.shutdownGracefully();
		}
	}
}
