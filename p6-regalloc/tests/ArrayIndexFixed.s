	.data
	.balign 8
ArrayIndexFixed.x:
	.zero 80
	.text
ArrayIndexFixed:
	pushq %rbp
	movq %rsp,%rbp
L.0:
	leaq ArrayIndexFixed.x(%rip),%rax
	movq 40(%rax),%rax
	movq %rax,ArrayIndexFixed.1.v
#	returnSink
	popq %rbp
	ret
ArrayIndexFixed.badSub:
	call badSub
