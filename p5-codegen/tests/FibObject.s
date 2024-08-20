	.data
	.balign 8
FibObject.Fib:
	.quad FibObject.Init
	.quad FibObject.Next
	.quad FibObject.NextN
	.quad FibObject.GetNum
	.quad FibObject.Print
	.text
FibObject.Next:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.41:
	xorq %rbx,%rbx
	movq %rbx,%rsi
L.1:
L.2:
L.3:
#	movq %rax,%rax
	xorq %rbx,%rbx
	cmpq %rbx,%rax
	je FibObject.Next.badPtr
L.4:
	movq 16(%rax),%rbx
	movq %rbx,%rsi
	movq %rax,%rdx
	xorq %rbx,%rbx
	cmpq %rbx,%rdx
	je FibObject.Next.badPtr
L.5:
	movq %rax,%rcx
	xorq %rbx,%rbx
	cmpq %rbx,%rcx
	je FibObject.Next.badPtr
L.6:
#	movq %rax,%rax
	xorq %rbx,%rbx
	cmpq %rbx,%rax
	je FibObject.Next.badPtr
L.7:
	movq 8(%rcx),%rbx
	movq 16(%rax),%rcx
#	movq %rbx,%rbx
	addq %rcx,%rbx
	movq %rbx,16(%rdx)
#	movq %rax,%rax
	xorq %rbx,%rbx
	cmpq %rbx,%rax
	je FibObject.Next.badPtr
L.8:
	movq %rsi,8(%rax)
	movq %rax,%rdx
	xorq %rbx,%rbx
	cmpq %rbx,%rdx
	je FibObject.Next.badPtr
L.9:
#	movq %rax,%rax
	xorq %rbx,%rbx
	cmpq %rbx,%rax
	je FibObject.Next.badPtr
L.10:
	movq 0(%rax),%rbx
	movq $1,%rcx
#	movq %rbx,%rbx
	addq %rcx,%rbx
	movq %rbx,0(%rdx)
#	movq %rax,%rax
	jmp L.0
L.42:
	jmp L.1
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
FibObject.Next.badPtr:
	call badPtr
FibObject.Next.badSub:
	call badSub
	.text
FibObject.NextN:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rsi,%r12
	movq %rdi,%rbx
L.43:
	xorq %rax,%rax
	movq %rax,%rcx
L.12:
	xorq %rax,%rax
	cmpq %rax,%r12
	jg L.13
L.14:
	movq %rcx,%rax
	jmp L.11
L.13:
	movq $1,%rcx
	movq %r12,%rax
	subq %rcx,%rax
	movq %rax,%r12
	movq -8(%rbx),%rax
	movq 8(%rax),%rax
	movq %rbx,%rdi
	call *%rax
	movq %rax,%rcx
	jmp L.12
L.11:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	addq $32,%rsp
	popq %rbp
	ret
FibObject.NextN.badSub:
	call badSub
	.data
	.balign 8
L.16:
	.byte 78
	.byte 117
	.byte 109
	.byte 98
	.byte 101
	.byte 114
	.byte 32
	.byte 116
	.byte 111
	.byte 32
	.byte 99
	.byte 97
	.byte 108
	.byte 99
	.byte 117
	.byte 108
	.byte 97
	.byte 116
	.byte 101
	.byte 32
	.byte 102
	.byte 105
	.byte 98
	.byte 32
	.byte 111
	.byte 102
	.byte 58
	.byte 32
	.byte 0
	.data
	.balign 8
L.17:
	.quad L.16
	.quad 29
	.text
FibObject.GetNum:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.44:
	xorq %rax,%rax
	movq %rax,%rbx
	leaq L.17(%rip),%rax
	movq %rax,%rdi
	call FibObject.CString
#	movq %rax,%rax
	movq %rax,%rdi
	xorq %rax,%rax
	movq %rax,%rsi
	xorq %rax,%rax
	movq %rax,%rdx
	call _printf
#	movq %rax,%rax
L.18:
L.19:
	call _getchar
	movq %rax,%rdx
	movq $10,%rax
	cmpq %rax,%rdx
	je L.21
L.23:
	movq $13,%rax
	cmpq %rax,%rdx
	je L.21
L.22:
	movq $10,%rax
	movq %rbx,%rcx
	imulq %rax,%rcx
	movq $48,%rax
	movq %rdx,%rbx
	subq %rax,%rbx
	movq %rcx,%rax
	addq %rbx,%rax
	movq %rax,%rbx
	jmp L.18
L.21:
L.20:
	movq %rbx,%rax
L.15:
#	returnSink
	movq -8(%rbp),%rbx
	addq $32,%rsp
	popq %rbp
	ret
FibObject.GetNum.badSub:
	call badSub
	.data
	.balign 8
L.25:
	.byte 70
	.byte 105
	.byte 98
	.byte 32
	.byte 37
	.byte 108
	.byte 100
	.byte 32
	.byte 105
	.byte 115
	.byte 32
	.byte 37
	.byte 108
	.byte 100
	.byte 10
	.byte 0
	.data
	.balign 8
L.26:
	.quad L.25
	.quad 16
	.text
FibObject.Print:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.45:
	leaq L.26(%rip),%rax
	movq %rax,%rdi
	call FibObject.CString
	movq %rax,%rdx
	movq %rbx,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je FibObject.Print.badPtr
L.27:
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je FibObject.Print.badPtr
L.28:
	movq %rdx,%rdi
	movq 0(%rcx),%rax
	movq %rax,%rsi
	movq 16(%rbx),%rax
	movq %rax,%rdx
	call _printf
#	movq %rax,%rax
L.24:
#	returnSink
	movq -8(%rbp),%rbx
	addq $32,%rsp
	popq %rbp
	ret
FibObject.Print.badPtr:
	call badPtr
FibObject.Print.badSub:
	call badSub
	.text
FibObject.Init:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.46:
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je FibObject.Init.badPtr
L.30:
	movq $1,%rax
	movq %rax,0(%rbx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je FibObject.Init.badPtr
L.31:
	xorq %rax,%rax
	movq %rax,8(%rbx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je FibObject.Init.badPtr
L.32:
	movq $1,%rax
	movq %rax,16(%rbx)
	movq %rbx,%rax
L.29:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
FibObject.Init.badPtr:
	call badPtr
FibObject.Init.badSub:
	call badSub
	.text
FibObject.CString:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.47:
#	movq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je FibObject.CString.badPtr
L.36:
	movq %rbx,%rcx
	xorq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl FibObject.CString.badSub
L.37:
	movq 8(%rcx),%rax
	cmpq %rax,%rbx
	jge FibObject.CString.badSub
L.38:
	movq 0(%rcx),%rax
#	movq %rax,%rax
	addq %rbx,%rax
#	movq %rax,%rax
L.33:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
FibObject.CString.badPtr:
	call badPtr
FibObject.CString.badSub:
	call badSub
	.text
FibObject:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
L.48:
	movq $32,%rax
	movq %rax,%rdi
	call _malloc
	movq %rax,%rcx
	movq $8,%rbx
	movq %rcx,%rax
	addq %rbx,%rax
	movq %rax,%rcx
	leaq FibObject.Fib(%rip),%rax
	movq %rax,-8(%rcx)
	xorq %rax,%rax
	movq %rax,0(%rcx)
	xorq %rax,%rax
	movq %rax,8(%rcx)
	xorq %rax,%rax
	movq %rax,16(%rcx)
	movq %rcx,%r12
	movq -8(%r12),%rax
	movq 0(%rax),%rax
	movq %r12,%rdi
	call *%rax
	movq %rax,%rbx
	movq -8(%r12),%rax
	movq 24(%rax),%rax
	movq %r12,%rdi
	call *%rax
	movq %rax,%rdx
	movq -8(%rbx),%rax
	movq 16(%rax),%rcx
	movq %rbx,%rdi
	movq $1,%rbx
	movq %rdx,%rax
	subq %rbx,%rax
	movq %rax,%rsi
	call *%rcx
	movq %rax,%rbx
	movq -8(%rbx),%rax
	movq 32(%rax),%rax
	movq %rbx,%rdi
	call *%rax
#	movq %rax,%rax
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	addq $32,%rsp
	popq %rbp
	ret
FibObject.badSub:
	call badSub
	.globl _main
	.text
_main:
	pushq %rbp
	movq %rsp,%rbp
L.49:
	call FibObject
#	movq %rax,%rax
	xorq %rax,%rax
#	movq %rax,%rax
#	returnSink
	popq %rbp
	ret
	.data
	.balign 8
L.39:
	.byte 65
	.byte 116
	.byte 116
	.byte 101
	.byte 109
	.byte 112
	.byte 116
	.byte 32
	.byte 116
	.byte 111
	.byte 32
	.byte 117
	.byte 115
	.byte 101
	.byte 32
	.byte 97
	.byte 32
	.byte 110
	.byte 117
	.byte 108
	.byte 108
	.byte 32
	.byte 112
	.byte 111
	.byte 105
	.byte 110
	.byte 116
	.byte 101
	.byte 114
	.byte 0
	.text
badPtr:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.50:
	leaq L.39(%rip),%rax
	movq %rax,%rdi
	call _puts
#	movq %rax,%rax
	movq $1,%rax
	movq %rax,%rdi
	call _exit
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
	.data
	.balign 8
L.40:
	.byte 83
	.byte 117
	.byte 98
	.byte 115
	.byte 99
	.byte 114
	.byte 105
	.byte 112
	.byte 116
	.byte 32
	.byte 111
	.byte 117
	.byte 116
	.byte 32
	.byte 111
	.byte 102
	.byte 32
	.byte 98
	.byte 111
	.byte 117
	.byte 110
	.byte 100
	.byte 115
	.byte 0
	.text
badSub:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.51:
	leaq L.40(%rip),%rax
	movq %rax,%rdi
	call _puts
#	movq %rax,%rax
	movq $1,%rax
	movq %rax,%rdi
	call _exit
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
