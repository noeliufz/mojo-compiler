	.text
HelloWorld.println:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.8:
#	movq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je HelloWorld.println.badPtr
L.3:
	movq %rbx,%rcx
	xorq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl HelloWorld.println.badSub
L.4:
	movq 8(%rcx),%rax
	cmpq %rax,%rbx
	jge HelloWorld.println.badSub
L.5:
	movq 0(%rcx),%rax
#	movq %rax,%rax
	addq %rbx,%rax
	movq %rax,%rdi
	call puts
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
HelloWorld.println.badPtr:
	call badPtr
HelloWorld.println.badSub:
	call badSub
	.data
	.balign 8
L.6:
	.byte 72
	.byte 101
	.byte 108
	.byte 108
	.byte 111
	.byte 32
	.byte 87
	.byte 111
	.byte 114
	.byte 108
	.byte 100
	.byte 0
	.data
	.balign 8
L.7:
	.quad L.6
	.quad 12
	.text
HelloWorld:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.9:
	leaq L.7(%rip),%rax
	movq %rax,%rdi
	call HelloWorld.println
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
