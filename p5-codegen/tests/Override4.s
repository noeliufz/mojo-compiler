	.data
	.balign 8
Override4.A:
	.quad Override4.FooA
	.data
	.balign 8
Override4.B:
	.quad Override4.FooB
	.data
	.balign 8
Override4.C:
	.quad Override4.FooC
	.text
Override4:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
