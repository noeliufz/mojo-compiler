	.data
	.balign 8
Override3-ok.A:
	.quad badPtr
	.data
	.balign 8
Override3-ok.AB:
	.quad badPtr
	.data
	.balign 8
Override3-ok.T1:
	.quad Override3-ok.Pab
	.data
	.balign 8
Override3-ok.T2:
	.quad Override3-ok.Pa
	.data
	.balign 8
Override3-ok.T3:
	.quad Override3-ok.Pa
	.text
Override3-ok:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
